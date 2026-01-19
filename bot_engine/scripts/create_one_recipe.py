#!/usr/bin/env python3
"""Create a single recipe from a bot with AI-generated images.

This script generates one AI-generated recipe using a bot persona,
complete with cover images generated via OpenAI DALL-E.

Usage:
    cd bot_engine
    python scripts/create_one_recipe.py                      # Random persona
    python scripts/create_one_recipe.py --persona chef_park_soojin  # Specific persona

Prerequisites:
    - Backend running at http://localhost:4000
    - OPENAI_API_KEY configured in .env
    - BOT_INTERNAL_SECRET configured in .env (matches backend)
"""

import argparse
import asyncio
import os
import random
import sys

# Add parent directory to path for imports
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# Load .env file for local development
from dotenv import load_dotenv
load_dotenv()

from src.api import CookstemmaClient
from src.config import get_settings
from src.generators import ImageGenerator, TextGenerator
from src.orchestrator.recipe_pipeline import RecipePipeline
from src.personas import get_persona_registry


def parse_args() -> argparse.Namespace:
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(
        description="Create a single AI-generated recipe from a bot persona"
    )
    parser.add_argument(
        "--persona",
        type=str,
        default=None,
        help="Persona name to use (e.g., chef_park_soojin). If not specified, picks random.",
    )
    parser.add_argument(
        "--food",
        type=str,
        default=None,
        help="Food name to create recipe for. If not specified, AI suggests one.",
    )
    parser.add_argument(
        "--no-images",
        action="store_true",
        help="Skip image generation (faster for testing)",
    )
    return parser.parse_args()


async def main() -> None:
    """Create a single AI-generated recipe with images."""
    args = parse_args()

    # Load settings
    settings = get_settings()

    # Check for OpenAI API key
    if not settings.openai_api_key or settings.openai_api_key == "sk-test-placeholder":
        print("Error: OPENAI_API_KEY not configured in .env")
        print("Add your OpenAI API key to bot_engine/.env:")
        print("  OPENAI_API_KEY=sk-your-key-here")
        sys.exit(1)

    # Check for internal secret
    if not settings.bot_internal_secret or settings.bot_internal_secret == "":
        print("Error: BOT_INTERNAL_SECRET not configured in .env")
        sys.exit(1)

    # Setup clients
    api_client = CookstemmaClient()
    text_gen = TextGenerator()
    image_gen = ImageGenerator()

    try:
        # Initialize persona registry from API
        print("Fetching personas from backend...")
        registry = get_persona_registry()
        await registry.initialize(api_client)

        all_personas = registry.get_all()
        if not all_personas:
            print("Error: No personas found in backend")
            sys.exit(1)

        print(f"Found {len(all_personas)} personas")

        # Select persona
        if args.persona:
            persona = registry.get(args.persona)
            if not persona:
                print(f"Error: Persona '{args.persona}' not found")
                print("Available personas:")
                for p in all_personas:
                    print(f"  - {p.name}")
                sys.exit(1)
        else:
            persona = random.choice(all_personas)

        print(f"\nUsing persona: {persona.name} ({persona.display_name.get('en', persona.name)})")

        # Authenticate with persona (auto-creates user if needed)
        print("Authenticating (will create user if needed)...")
        auth = await api_client.login_by_persona(persona.name)
        print(f"Authenticated as: {auth.username}")

        # Update persona with auth info
        persona.user_public_id = auth.user_public_id
        persona.persona_public_id = auth.persona_public_id

        # Create pipeline
        pipeline = RecipePipeline(api_client, text_gen, image_gen)

        # Get existing foods to exclude from suggestions
        print("\nFetching existing foods...")
        existing_foods = await api_client.get_created_foods()
        print(f"Bot has created {len(existing_foods)} foods already")

        # Determine food name
        if args.food:
            food_name = args.food
            print(f"Using specified food: {food_name}")
        else:
            # Ask AI to suggest a food name
            print("Asking AI for food suggestion...")
            suggestions = await text_gen.suggest_food_names(
                persona=persona,
                count=1,
                exclude=existing_foods,
            )

            if not suggestions:
                print("Error: AI couldn't suggest any new foods")
                sys.exit(1)

            # Filter out any that somehow still match existing (case-insensitive)
            existing_lower = {f.lower() for f in existing_foods}
            filtered = [f for f in suggestions if f.lower() not in existing_lower]

            if not filtered:
                print("Error: All suggested foods already exist")
                sys.exit(1)

            food_name = filtered[0]
            print(f"AI chose: {food_name}")

        # Generate recipe
        generate_images = not args.no_images
        if generate_images:
            print("\nThis may take a minute (generating text + 1 cover image)...")
        else:
            print("\nGenerating recipe (no images)...")

        recipe = await pipeline.generate_original_recipe(
            persona=persona,
            food_name=food_name,
            generate_images=generate_images,
            cover_image_count=1 if generate_images else 0,
            generate_step_images=False,
        )

        # Print results
        print("\n" + "=" * 50)
        print("Recipe created successfully!")
        print("=" * 50)
        print(f"Public ID: {recipe.public_id}")
        print(f"Title: {recipe.title}")
        print(f"Description: {recipe.description[:100]}...")
        print(f"Ingredients: {len(recipe.ingredients)} items")
        print(f"Steps: {len(recipe.steps)} steps")
        print(f"Images: {len(recipe.images)} cover images")

        if recipe.images:
            print("\nCover Image URLs:")
            for i, img in enumerate(recipe.images, 1):
                print(f"  {i}. {img.image_url}")

        # Show step images
        steps_with_images = [s for s in recipe.steps if s.image_public_id]
        if steps_with_images:
            print(f"\nSteps with images: {len(steps_with_images)}")
            for step in steps_with_images:
                print(f"  Step {step.step_number}: image_id={step.image_public_id}")

        print("\nView in app or backend logs for full details.")

    except Exception as e:
        print(f"\nError: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

    finally:
        # Cleanup
        await api_client.close()
        await image_gen.close()


if __name__ == "__main__":
    asyncio.run(main())
