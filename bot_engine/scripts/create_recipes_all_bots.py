#!/usr/bin/env python3
"""Create one recipe for each bot persona with concurrent processing.

This script generates one AI-generated recipe for each of the 43 bot personas,
auto-creating bot users as needed. Supports concurrent processing for faster execution.

Usage:
    cd bot_engine
    python scripts/create_recipes_all_bots.py
    python scripts/create_recipes_all_bots.py --concurrency 10  # 10 concurrent requests
    python scripts/create_recipes_all_bots.py --step-images     # Include step images
    python scripts/create_recipes_all_bots.py --limit 5         # Only first 5 personas

Prerequisites:
    - Backend running at http://localhost:4000
    - OPENAI_API_KEY configured in .env
    - BOT_INTERNAL_SECRET configured in .env (matches backend)
"""

import argparse
import asyncio
import os
import sys
from dataclasses import dataclass
from typing import List, Optional

# Add parent directory to path for imports
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# Load .env file for local development
from dotenv import load_dotenv
load_dotenv()

from src.api import CookstemmaClient
from src.config import get_settings
from src.generators import ImageGenerator, TextGenerator
from src.orchestrator.recipe_pipeline import RecipePipeline
from src.personas import BotPersona, get_persona_registry


@dataclass
class RecipeResult:
    """Result of recipe creation for a persona."""
    persona_name: str
    success: bool
    recipe_id: Optional[str] = None
    recipe_title: Optional[str] = None
    error: Optional[str] = None


def parse_args() -> argparse.Namespace:
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(
        description="Create one recipe for each bot persona"
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=None,
        help="Limit to first N personas (for testing)",
    )
    parser.add_argument(
        "--step-images",
        action="store_true",
        help="Generate images for each recipe step (default: cover image only)",
    )
    parser.add_argument(
        "--cover",
        type=int,
        choices=[1, 2, 3],
        default=1,
        help="Number of cover images to generate (1, 2, or 3)",
    )
    parser.add_argument(
        "--continue-on-error",
        action="store_true",
        default=True,
        help="Continue processing other personas if one fails (default: True)",
    )
    parser.add_argument(
        "--concurrency",
        type=int,
        default=5,
        help="Number of concurrent recipe generations (default: 5, max recommended: 10)",
    )
    parser.add_argument(
        "--sequential",
        action="store_true",
        help="Run sequentially instead of concurrently (for debugging)",
    )
    parser.add_argument(
        "--batch-size",
        type=int,
        default=5,
        help="Number of recipes per batch before longer pause (sequential mode only, default: 5)",
    )
    parser.add_argument(
        "--delay",
        type=int,
        default=3,
        help="Seconds to wait between recipes (sequential mode only, default: 3)",
    )
    parser.add_argument(
        "--batch-delay",
        type=int,
        default=15,
        help="Seconds to wait between batches (sequential mode only, default: 15)",
    )
    return parser.parse_args()


async def create_recipe_for_persona(
    persona: BotPersona,
    api_client: CookstemmaClient,
    text_gen: TextGenerator,
    image_gen: ImageGenerator,
    generate_step_images: bool,
    cover_image_count: int = 1,
    semaphore: asyncio.Semaphore | None = None,
    index: int = 0,
    total: int = 0,
) -> RecipeResult:
    """Create a recipe for a single persona (cover image always generated).

    Args:
        semaphore: Optional semaphore for controlling concurrency
        index: Current index for progress display
        total: Total count for progress display
    """
    async def _do_create() -> RecipeResult:
        prefix = f"[{index}/{total}]" if total > 0 else ""
        print(f"{prefix} {persona.name} ({persona.display_name.get('en', '')}) - Starting...")

        try:
            # Authenticate with persona (auto-creates user if needed)
            auth = await api_client.login_by_persona(persona.name)
            print(f"  {prefix} Authenticated as: {auth.username}")

            # Update persona with auth info
            persona.user_public_id = auth.user_public_id
            persona.persona_public_id = auth.persona_public_id

            # Get existing foods to exclude
            existing_foods = await api_client.get_created_foods()

            # Ask AI to suggest a food name
            suggestions = await text_gen.suggest_food_names(
                persona=persona,
                count=1,
                exclude=existing_foods,
            )

            if not suggestions:
                return RecipeResult(
                    persona_name=persona.name,
                    success=False,
                    error="AI couldn't suggest any new foods",
                )

            # Filter out duplicates
            existing_lower = {f.lower() for f in existing_foods}
            filtered = [f for f in suggestions if f.lower() not in existing_lower]

            if not filtered:
                return RecipeResult(
                    persona_name=persona.name,
                    success=False,
                    error="All suggested foods already exist",
                )

            food_name = filtered[0]
            print(f"  {prefix} Creating recipe for: {food_name}")

            # Create pipeline and generate recipe
            pipeline = RecipePipeline(api_client, text_gen, image_gen)
            recipe = await pipeline.generate_original_recipe(
                persona=persona,
                food_name=food_name,
                generate_images=True,
                cover_image_count=cover_image_count,
                generate_step_images=generate_step_images,
            )

            print(f"  {prefix} ✓ Created: {recipe.title}")
            return RecipeResult(
                persona_name=persona.name,
                success=True,
                recipe_id=recipe.public_id,
                recipe_title=recipe.title,
            )

        except Exception as e:
            print(f"  {prefix} ✗ Failed: {e}")
            return RecipeResult(
                persona_name=persona.name,
                success=False,
                error=str(e),
            )

    # Use semaphore if provided for concurrency control
    if semaphore:
        async with semaphore:
            return await _do_create()
    else:
        return await _do_create()


async def run_sequential(
    personas: List[BotPersona],
    api_client: CookstemmaClient,
    text_gen: TextGenerator,
    image_gen: ImageGenerator,
    args: argparse.Namespace,
) -> List[RecipeResult]:
    """Run recipe creation sequentially with rate limiting."""
    results: List[RecipeResult] = []
    total = len(personas)

    batch_size = args.batch_size
    delay_between_recipes = args.delay
    delay_between_batches = args.batch_delay
    print(f"Sequential mode: {delay_between_recipes}s between recipes, {delay_between_batches}s between batches of {batch_size}")
    print("=" * 60)

    for i, persona in enumerate(personas, 1):
        result = await create_recipe_for_persona(
            persona=persona,
            api_client=api_client,
            text_gen=text_gen,
            image_gen=image_gen,
            generate_step_images=args.step_images,
            cover_image_count=args.cover,
            index=i,
            total=total,
        )
        results.append(result)

        if not result.success and not args.continue_on_error:
            print("\nStopping due to error (use --continue-on-error to skip)")
            break

        # Rate limiting: delay between recipes
        if i < total:  # Don't delay after the last recipe
            if i % batch_size == 0:
                print(f"\n  ⏳ Batch complete. Waiting {delay_between_batches}s before next batch...")
                await asyncio.sleep(delay_between_batches)
            else:
                await asyncio.sleep(delay_between_recipes)

    return results


async def run_concurrent(
    personas: List[BotPersona],
    api_client: CookstemmaClient,
    text_gen: TextGenerator,
    image_gen: ImageGenerator,
    args: argparse.Namespace,
) -> List[RecipeResult]:
    """Run recipe creation concurrently with semaphore-based rate limiting."""
    total = len(personas)
    concurrency = min(args.concurrency, total)  # Don't exceed persona count

    print(f"Concurrent mode: {concurrency} concurrent tasks for {total} personas")
    print("=" * 60)

    semaphore = asyncio.Semaphore(concurrency)

    # Create all tasks
    tasks = [
        create_recipe_for_persona(
            persona=persona,
            api_client=api_client,
            text_gen=text_gen,
            image_gen=image_gen,
            generate_step_images=args.step_images,
            cover_image_count=args.cover,
            semaphore=semaphore,
            index=i,
            total=total,
        )
        for i, persona in enumerate(personas, 1)
    ]

    # Run all tasks concurrently (semaphore controls actual concurrency)
    results = await asyncio.gather(*tasks, return_exceptions=True)

    # Convert exceptions to RecipeResult failures
    processed_results: List[RecipeResult] = []
    for i, result in enumerate(results):
        if isinstance(result, Exception):
            processed_results.append(RecipeResult(
                persona_name=personas[i].name,
                success=False,
                error=str(result),
            ))
        else:
            processed_results.append(result)

    return processed_results


async def main() -> None:
    """Create one recipe for each bot persona."""
    args = parse_args()

    # Load settings
    settings = get_settings()

    # Check for OpenAI API key
    if not settings.openai_api_key or settings.openai_api_key == "sk-test-placeholder":
        print("Error: OPENAI_API_KEY not configured in .env")
        sys.exit(1)

    # Check for internal secret
    if not settings.bot_internal_secret or settings.bot_internal_secret == "":
        print("Error: BOT_INTERNAL_SECRET not configured in .env")
        sys.exit(1)

    # Setup clients
    api_client = CookstemmaClient()
    text_gen = TextGenerator()
    image_gen = ImageGenerator()

    results: List[RecipeResult] = []

    try:
        # Initialize persona registry from API
        print("Fetching personas from backend...")
        registry = get_persona_registry()
        await registry.initialize(api_client)

        all_personas = registry.get_all()
        if not all_personas:
            print("Error: No personas found in backend")
            sys.exit(1)

        # Apply limit if specified
        personas_to_process = all_personas
        if args.limit:
            personas_to_process = all_personas[:args.limit]

        total = len(personas_to_process)
        print(f"\nProcessing {total} personas...")

        # Choose execution mode
        if args.sequential:
            results = await run_sequential(
                personas_to_process, api_client, text_gen, image_gen, args
            )
        else:
            results = await run_concurrent(
                personas_to_process, api_client, text_gen, image_gen, args
            )

        # Print summary
        print("\n" + "=" * 60)
        print("SUMMARY")
        print("=" * 60)

        successful = [r for r in results if r.success]
        failed = [r for r in results if not r.success]

        print(f"\nTotal: {len(results)}")
        print(f"Successful: {len(successful)}")
        print(f"Failed: {len(failed)}")

        if successful:
            print("\nSuccessful recipes:")
            for r in successful:
                print(f"  - {r.persona_name}: {r.recipe_title} ({r.recipe_id})")

        if failed:
            print("\nFailed personas:")
            for r in failed:
                print(f"  - {r.persona_name}: {r.error}")

    except Exception as e:
        print(f"\nFatal error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

    finally:
        # Cleanup
        await api_client.close()
        await image_gen.close()

    # Exit with error code if any failures
    if failed:
        sys.exit(1)


if __name__ == "__main__":
    asyncio.run(main())
