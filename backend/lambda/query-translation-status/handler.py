"""
Query Translation Status Lambda
Quick Lambda to check translation status for a specific recipe.
"""
import json
import logging
import os
import boto3
import psycopg2
from psycopg2.extras import RealDictCursor

logger = logging.getLogger()
logger.setLevel(logging.INFO)

secrets_client = boto3.client('secretsmanager')
_secrets_cache = {}


def get_secret(secret_name: str) -> dict:
    """Retrieve and cache secrets from AWS Secrets Manager."""
    if secret_name not in _secrets_cache:
        response = secrets_client.get_secret_value(SecretId=secret_name)
        _secrets_cache[secret_name] = json.loads(response['SecretString'])
    return _secrets_cache[secret_name]


def get_db_connection():
    """Create database connection using secrets."""
    db_secret = get_secret(os.environ['DATABASE_SECRET_ARN'])

    return psycopg2.connect(
        host=db_secret['host'],
        port=db_secret['port'],
        dbname=db_secret['dbname'],
        user=db_secret['username'],
        password=db_secret['password'],
        cursor_factory=RealDictCursor
    )


def lambda_handler(event, context):
    """
    Query translation status for a recipe by public_id.

    Example event:
    {
        "recipe_public_id": "a02df326-1da6-4ad5-9252-e3b0f6fe40d9"
    }
    """
    try:
        # Get recipe public_id from event
        recipe_public_id = event.get('recipe_public_id')
        if not recipe_public_id:
            return {
                'statusCode': 400,
                'body': json.dumps({'error': 'recipe_public_id is required'})
            }

        logger.info(f"Querying translation status for recipe: {recipe_public_id}")

        conn = get_db_connection()

        try:
            with conn.cursor() as cur:
                # Find the recipe
                cur.execute("""
                    SELECT id, public_id, title, created_at
                    FROM recipes
                    WHERE public_id = %s
                """, (recipe_public_id,))

                recipe = cur.fetchone()

                if not recipe:
                    return {
                        'statusCode': 404,
                        'body': json.dumps({'error': 'Recipe not found'})
                    }

                recipe_id = recipe['id']
                logger.info(f"Found recipe: id={recipe_id}, title={recipe['title']}")

                # Check translation events
                cur.execute("""
                    SELECT
                        te.id,
                        te.status,
                        te.entity_type,
                        te.entity_id,
                        te.source_locale,
                        te.target_locales,
                        te.completed_locales,
                        te.error_message,
                        te.retry_count,
                        te.started_at,
                        te.completed_at,
                        te.created_at,
                        EXTRACT(EPOCH FROM (NOW() - te.started_at)) as time_elapsed_seconds
                    FROM translation_events te
                    WHERE te.entity_id = %s
                        AND te.entity_type IN ('RECIPE', 'RECIPE_FULL')
                    ORDER BY te.created_at DESC
                    LIMIT 10
                """, (recipe_id,))

                events = cur.fetchall()

                # Format the response
                result = {
                    'recipe': {
                        'id': recipe['id'],
                        'public_id': str(recipe['public_id']),
                        'title': recipe['title'],
                        'created_at': recipe['created_at'].isoformat()
                    },
                    'translation_events': []
                }

                for event in events:
                    result['translation_events'].append({
                        'id': event['id'],
                        'status': event['status'],
                        'entity_type': event['entity_type'],
                        'source_locale': event['source_locale'],
                        'target_locales': event['target_locales'],
                        'completed_locales': event['completed_locales'] or [],
                        'completed_count': len(event['completed_locales']) if event['completed_locales'] else 0,
                        'target_count': len(event['target_locales']) if event['target_locales'] else 0,
                        'error_message': event['error_message'],
                        'retry_count': event['retry_count'],
                        'started_at': event['started_at'].isoformat() if event['started_at'] else None,
                        'completed_at': event['completed_at'].isoformat() if event['completed_at'] else None,
                        'created_at': event['created_at'].isoformat(),
                        'time_elapsed_seconds': int(event['time_elapsed_seconds']) if event['time_elapsed_seconds'] else None
                    })

                logger.info(f"Found {len(events)} translation events")

                return {
                    'statusCode': 200,
                    'body': json.dumps(result, indent=2)
                }

        finally:
            conn.close()

    except Exception as e:
        logger.error(f"Error querying translation status: {e}", exc_info=True)
        return {
            'statusCode': 500,
            'body': json.dumps({'error': str(e)})
        }
