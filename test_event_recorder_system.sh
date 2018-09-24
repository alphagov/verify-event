#!/usr/bin/env bash

usage() {
  echo "Usage: $0 -e [default|test|staging|integration|prod] -m <multifactor code> -p <AWS profile>" 1>&2;
  exit 1;
}

TERRAFORM_DIRECTORY=../verify-event-infrastructure
if [ ! -d "$TERRAFORM_DIRECTORY" ]; then
  echo "Please git clone --depth=1 git@github.com:alphagov/verify-event-infrastructure.git $TERRAFORM_DIRECTORY"
fi

shift $((OPTIND-1))

while getopts ":e:m:p:" option; do
    case $option in
        e) ENVIRONMENT=$OPTARG ;;
        m) MFA=$OPTARG ;;
        p) PROFILE=$OPTARG ;;
        *) usage ;;
    esac
done

shift $((OPTIND-1))

if [ -z "$ENVIRONMENT" ] || [ -z "$MFA" ] || [ -z "$PROFILE" ]; then
    usage
fi

if [ "$ENVIRONMENT" != "default" ] && [ "$ENVIRONMENT" != "test" ] && [ "$ENVIRONMENT" != "staging" ] && [ "$ENVIRONMENT" != "integration" ] && [ "$ENVIRONMENT" != "prod" ]; then
    echo "Invalid environment"
    usage
fi

cd ../verify-event-infrastructure
eval $(tools/assume-role.py -t $PROFILE -m $MFA)
cd environments/$ENVIRONMENT
terraform init
PREFIX_API_GATEWAY_URL=`terraform state show module.recording_system.aws_api_gateway_stage.event-api-stage | awk '$1 == "invoke_url" {print $3}'`
SUFFIX_API_GATEWAY_URL=`terraform state show module.recording_system.aws_api_gateway_resource.event-api-auditevents-resource | awk '$1 == "path" {print $3}'`
export EVENT_EMITTER_ENABLED=true
export EVENT_EMITTER_ASSUME_ROLE=`terraform state show module.recording_system.aws_iam_role.event_tester | awk '$1 == "arn" {print $3}'`
export EVENT_EMITTER_API_GATEWAY_URL=$PREFIX_API_GATEWAY_URL$SUFFIX_API_GATEWAY_URL
export EVENT_STORE_USERNAME=writer
export EVENT_STORE_PASSWORD=`terraform state show module.recording_system.postgresql_role.writer | awk '$1 == "password" {print $3}'`
export EVENT_EMITTER_ENCRYPTION_KEY=`terraform state show module.recording_system.aws_s3_bucket_object.event_encryption_key | awk '$1 == "content" {print $3}'`
export EVENT_STORE_HOSTNAME=`terraform state show module.recording_system.aws_db_instance.event-store | awk '$1 == "address" {print $3}'`
export EVENT_STORE_DB_TRUSTSTORE_PASSWORD=`terraform state show module.recording_system.aws_s3_bucket_object.database_truststore_password | awk '$1 == "content" {print $3}'`
cd ../../../verify-event
./gradlew clean eventsTest

