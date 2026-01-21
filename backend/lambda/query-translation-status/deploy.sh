#!/bin/bash
# Deploy query-translation-status Lambda to AWS
# Usage: ./deploy.sh [environment]

set -e

ENVIRONMENT=${1:-dev}
AWS_REGION=${AWS_REGION:-us-east-2}
PROJECT_NAME="cookstemma"
LAMBDA_NAME="${PROJECT_NAME}-${ENVIRONMENT}-query-translation"
ECR_REPO="${PROJECT_NAME}-${ENVIRONMENT}-query-translation"

echo "Deploying query-translation-status Lambda to ${ENVIRONMENT}..."

# Get AWS account ID
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_URL="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO}"

echo "ECR URL: ${ECR_URL}"

# Create ECR repository if it doesn't exist
echo "Creating ECR repository if needed..."
aws ecr describe-repositories --repository-names ${ECR_REPO} --region ${AWS_REGION} 2>/dev/null || \
    aws ecr create-repository --repository-name ${ECR_REPO} --region ${AWS_REGION}

# Login to ECR
echo "Logging into ECR..."
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

# Build Docker image
echo "Building Docker image..."
docker build -t ${ECR_REPO}:latest .

# Tag for ECR
echo "Tagging image for ECR..."
docker tag ${ECR_REPO}:latest ${ECR_URL}:latest

# Push to ECR
echo "Pushing image to ECR..."
docker push ${ECR_URL}:latest

# Check if Lambda exists
if aws lambda get-function --function-name ${LAMBDA_NAME} --region ${AWS_REGION} 2>/dev/null; then
    echo "Updating existing Lambda function..."
    aws lambda update-function-code \
        --function-name ${LAMBDA_NAME} \
        --image-uri ${ECR_URL}:latest \
        --region ${AWS_REGION}
else
    echo "Lambda function does not exist. Please create it first via AWS Console or infrastructure code."
    echo "Required configuration:"
    echo "  - Function name: ${LAMBDA_NAME}"
    echo "  - Image URI: ${ECR_URL}:latest"
    echo "  - Environment variable: DATABASE_SECRET_ARN=arn:aws:secretsmanager:${AWS_REGION}:${AWS_ACCOUNT_ID}:secret:cookstemma/dev/database-XXXXXX"
    echo "  - IAM role: Must have secretsmanager:GetSecretValue permission"
    exit 1
fi

echo "Waiting for Lambda to update..."
aws lambda wait function-updated \
    --function-name ${LAMBDA_NAME} \
    --region ${AWS_REGION}

echo "Deployment complete!"
echo "Lambda ARN: arn:aws:lambda:${AWS_REGION}:${AWS_ACCOUNT_ID}:function:${LAMBDA_NAME}"
echo ""
echo "To invoke:"
echo "aws lambda invoke --function-name ${LAMBDA_NAME} --region ${AWS_REGION} --payload '{\"recipe_public_id\":\"a02df326-1da6-4ad5-9252-e3b0f6fe40d9\"}' output.json"
