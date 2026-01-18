/*
 * Copyright 2023 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.otel4s
package semconv
package experimental.attributes

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/SemanticAttributes.scala.j2
object AwsExperimentalAttributes {

  /** The unique identifier of the AWS Bedrock Guardrail. A <a
    * href="https://docs.aws.amazon.com/bedrock/latest/userguide/guardrails.html">guardrail</a> helps safeguard and
    * prevent unwanted behavior from model responses or user messages.
    */
  val AwsBedrockGuardrailId: AttributeKey[String] =
    AttributeKey("aws.bedrock.guardrail.id")

  /** The unique identifier of the AWS Bedrock Knowledge base. A <a
    * href="https://docs.aws.amazon.com/bedrock/latest/userguide/knowledge-base.html">knowledge base</a> is a bank of
    * information that can be queried by models to generate more relevant responses and augment prompts.
    */
  val AwsBedrockKnowledgeBaseId: AttributeKey[String] =
    AttributeKey("aws.bedrock.knowledge_base.id")

  /** The JSON-serialized value of each item in the `AttributeDefinitions` request field.
    */
  val AwsDynamodbAttributeDefinitions: AttributeKey[Seq[String]] =
    AttributeKey("aws.dynamodb.attribute_definitions")

  /** The value of the `AttributesToGet` request parameter.
    */
  val AwsDynamodbAttributesToGet: AttributeKey[Seq[String]] =
    AttributeKey("aws.dynamodb.attributes_to_get")

  /** The value of the `ConsistentRead` request parameter.
    */
  val AwsDynamodbConsistentRead: AttributeKey[Boolean] =
    AttributeKey("aws.dynamodb.consistent_read")

  /** The JSON-serialized value of each item in the `ConsumedCapacity` response field.
    */
  val AwsDynamodbConsumedCapacity: AttributeKey[Seq[String]] =
    AttributeKey("aws.dynamodb.consumed_capacity")

  /** The value of the `Count` response parameter.
    */
  val AwsDynamodbCount: AttributeKey[Long] =
    AttributeKey("aws.dynamodb.count")

  /** The value of the `ExclusiveStartTableName` request parameter.
    */
  val AwsDynamodbExclusiveStartTable: AttributeKey[String] =
    AttributeKey("aws.dynamodb.exclusive_start_table")

  /** The JSON-serialized value of each item in the `GlobalSecondaryIndexUpdates` request field.
    */
  val AwsDynamodbGlobalSecondaryIndexUpdates: AttributeKey[Seq[String]] =
    AttributeKey("aws.dynamodb.global_secondary_index_updates")

  /** The JSON-serialized value of each item of the `GlobalSecondaryIndexes` request field
    */
  val AwsDynamodbGlobalSecondaryIndexes: AttributeKey[Seq[String]] =
    AttributeKey("aws.dynamodb.global_secondary_indexes")

  /** The value of the `IndexName` request parameter.
    */
  val AwsDynamodbIndexName: AttributeKey[String] =
    AttributeKey("aws.dynamodb.index_name")

  /** The JSON-serialized value of the `ItemCollectionMetrics` response field.
    */
  val AwsDynamodbItemCollectionMetrics: AttributeKey[String] =
    AttributeKey("aws.dynamodb.item_collection_metrics")

  /** The value of the `Limit` request parameter.
    */
  val AwsDynamodbLimit: AttributeKey[Long] =
    AttributeKey("aws.dynamodb.limit")

  /** The JSON-serialized value of each item of the `LocalSecondaryIndexes` request field.
    */
  val AwsDynamodbLocalSecondaryIndexes: AttributeKey[Seq[String]] =
    AttributeKey("aws.dynamodb.local_secondary_indexes")

  /** The value of the `ProjectionExpression` request parameter.
    */
  val AwsDynamodbProjection: AttributeKey[String] =
    AttributeKey("aws.dynamodb.projection")

  /** The value of the `ProvisionedThroughput.ReadCapacityUnits` request parameter.
    */
  val AwsDynamodbProvisionedReadCapacity: AttributeKey[Double] =
    AttributeKey("aws.dynamodb.provisioned_read_capacity")

  /** The value of the `ProvisionedThroughput.WriteCapacityUnits` request parameter.
    */
  val AwsDynamodbProvisionedWriteCapacity: AttributeKey[Double] =
    AttributeKey("aws.dynamodb.provisioned_write_capacity")

  /** The value of the `ScanIndexForward` request parameter.
    */
  val AwsDynamodbScanForward: AttributeKey[Boolean] =
    AttributeKey("aws.dynamodb.scan_forward")

  /** The value of the `ScannedCount` response parameter.
    */
  val AwsDynamodbScannedCount: AttributeKey[Long] =
    AttributeKey("aws.dynamodb.scanned_count")

  /** The value of the `Segment` request parameter.
    */
  val AwsDynamodbSegment: AttributeKey[Long] =
    AttributeKey("aws.dynamodb.segment")

  /** The value of the `Select` request parameter.
    */
  val AwsDynamodbSelect: AttributeKey[String] =
    AttributeKey("aws.dynamodb.select")

  /** The number of items in the `TableNames` response parameter.
    */
  val AwsDynamodbTableCount: AttributeKey[Long] =
    AttributeKey("aws.dynamodb.table_count")

  /** The keys in the `RequestItems` object field.
    */
  val AwsDynamodbTableNames: AttributeKey[Seq[String]] =
    AttributeKey("aws.dynamodb.table_names")

  /** The value of the `TotalSegments` request parameter.
    */
  val AwsDynamodbTotalSegments: AttributeKey[Long] =
    AttributeKey("aws.dynamodb.total_segments")

  /** The ARN of an <a href="https://docs.aws.amazon.com/AmazonECS/latest/developerguide/clusters.html">ECS cluster</a>.
    */
  val AwsEcsClusterArn: AttributeKey[String] =
    AttributeKey("aws.ecs.cluster.arn")

  /** The Amazon Resource Name (ARN) of an <a
    * href="https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ECS_instances.html">ECS container instance</a>.
    */
  val AwsEcsContainerArn: AttributeKey[String] =
    AttributeKey("aws.ecs.container.arn")

  /** The <a href="https://docs.aws.amazon.com/AmazonECS/latest/developerguide/launch_types.html">launch type</a> for an
    * ECS task.
    */
  val AwsEcsLaunchtype: AttributeKey[String] =
    AttributeKey("aws.ecs.launchtype")

  /** The ARN of a running <a
    * href="https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ecs-account-settings.html#ecs-resource-ids">ECS
    * task</a>.
    */
  val AwsEcsTaskArn: AttributeKey[String] =
    AttributeKey("aws.ecs.task.arn")

  /** The family name of the <a
    * href="https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task_definitions.html">ECS task definition</a>
    * used to create the ECS task.
    */
  val AwsEcsTaskFamily: AttributeKey[String] =
    AttributeKey("aws.ecs.task.family")

  /** The ID of a running ECS task. The ID MUST be extracted from `task.arn`.
    */
  val AwsEcsTaskId: AttributeKey[String] =
    AttributeKey("aws.ecs.task.id")

  /** The revision for the task definition used to create the ECS task.
    */
  val AwsEcsTaskRevision: AttributeKey[String] =
    AttributeKey("aws.ecs.task.revision")

  /** The ARN of an EKS cluster.
    */
  val AwsEksClusterArn: AttributeKey[String] =
    AttributeKey("aws.eks.cluster.arn")

  /** The AWS extended request ID as returned in the response header `x-amz-id-2`.
    */
  val AwsExtendedRequestId: AttributeKey[String] =
    AttributeKey("aws.extended_request_id")

  /** The name of the AWS Kinesis <a href="https://docs.aws.amazon.com/streams/latest/dev/introduction.html">stream</a>
    * the request refers to. Corresponds to the `--stream-name` parameter of the Kinesis <a
    * href="https://docs.aws.amazon.com/cli/latest/reference/kinesis/describe-stream.html">describe-stream</a>
    * operation.
    */
  val AwsKinesisStreamName: AttributeKey[String] =
    AttributeKey("aws.kinesis.stream_name")

  /** The full invoked ARN as provided on the `Context` passed to the function (`Lambda-Runtime-Invoked-Function-Arn`
    * header on the `/runtime/invocation/next` applicable).
    *
    * @note
    *   <p> This may be different from `cloud.resource_id` if an alias is involved.
    */
  val AwsLambdaInvokedArn: AttributeKey[String] =
    AttributeKey("aws.lambda.invoked_arn")

  /** The UUID of the <a
    * href="https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-lambda-eventsourcemapping.html">AWS
    * Lambda EvenSource Mapping</a>. An event source is mapped to a lambda function. It's contents are read by Lambda
    * and used to trigger a function. This isn't available in the lambda execution context or the lambda runtime
    * environtment. This is going to be populated by the AWS SDK for each language when that UUID is present. Some of
    * these operations are Create/Delete/Get/List/Update EventSourceMapping.
    */
  val AwsLambdaResourceMappingId: AttributeKey[String] =
    AttributeKey("aws.lambda.resource_mapping.id")

  /** The Amazon Resource Name(s) (ARN) of the AWS log group(s).
    *
    * @note
    *   <p> See the <a
    *   href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/iam-access-control-overview-cwl.html#CWL_ARN_Format">log
    *   group ARN format documentation</a>.
    */
  val AwsLogGroupArns: AttributeKey[Seq[String]] =
    AttributeKey("aws.log.group.arns")

  /** The name(s) of the AWS log group(s) an application is writing to.
    *
    * @note
    *   <p> Multiple log groups must be supported for cases like multi-container applications, where a single
    *   application has sidecar containers, and each write to their own log group.
    */
  val AwsLogGroupNames: AttributeKey[Seq[String]] =
    AttributeKey("aws.log.group.names")

  /** The ARN(s) of the AWS log stream(s).
    *
    * @note
    *   <p> See the <a
    *   href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/iam-access-control-overview-cwl.html#CWL_ARN_Format">log
    *   stream ARN format documentation</a>. One log group can contain several log streams, so these ARNs necessarily
    *   identify both a log group and a log stream.
    */
  val AwsLogStreamArns: AttributeKey[Seq[String]] =
    AttributeKey("aws.log.stream.arns")

  /** The name(s) of the AWS log stream(s) an application is writing to.
    */
  val AwsLogStreamNames: AttributeKey[Seq[String]] =
    AttributeKey("aws.log.stream.names")

  /** The AWS request ID as returned in the response headers `x-amzn-requestid`, `x-amzn-request-id` or
    * `x-amz-request-id`.
    */
  val AwsRequestId: AttributeKey[String] =
    AttributeKey("aws.request_id")

  /** The S3 bucket name the request refers to. Corresponds to the `--bucket` parameter of the <a
    * href="https://docs.aws.amazon.com/cli/latest/reference/s3api/index.html">S3 API</a> operations.
    *
    * @note
    *   <p> The `bucket` attribute is applicable to all S3 operations that reference a bucket, i.e. that require the
    *   bucket name as a mandatory parameter. This applies to almost all S3 operations except `list-buckets`.
    */
  val AwsS3Bucket: AttributeKey[String] =
    AttributeKey("aws.s3.bucket")

  /** The source object (in the form `bucket`/`key`) for the copy operation.
    *
    * @note
    *   <p> The `copy_source` attribute applies to S3 copy operations and corresponds to the `--copy-source` parameter
    *   of the <a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/copy-object.html">copy-object operation
    *   within the S3 API</a>. This applies in particular to the following operations: <ul> <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/copy-object.html">copy-object</a> <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/upload-part-copy.html">upload-part-copy</a> </ul>
    */
  val AwsS3CopySource: AttributeKey[String] =
    AttributeKey("aws.s3.copy_source")

  /** The delete request container that specifies the objects to be deleted.
    *
    * @note
    *   <p> The `delete` attribute is only applicable to the <a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/delete-object.html">delete-object</a> operation.
    *   The `delete` attribute corresponds to the `--delete` parameter of the <a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/delete-objects.html">delete-objects operation
    *   within the S3 API</a>.
    */
  val AwsS3Delete: AttributeKey[String] =
    AttributeKey("aws.s3.delete")

  /** The S3 object key the request refers to. Corresponds to the `--key` parameter of the <a
    * href="https://docs.aws.amazon.com/cli/latest/reference/s3api/index.html">S3 API</a> operations.
    *
    * @note
    *   <p> The `key` attribute is applicable to all object-related S3 operations, i.e. that require the object key as a
    *   mandatory parameter. This applies in particular to the following operations: <ul> <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/copy-object.html">copy-object</a> <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/delete-object.html">delete-object</a> <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/get-object.html">get-object</a> <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/head-object.html">head-object</a> <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/put-object.html">put-object</a> <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/restore-object.html">restore-object</a> <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/select-object-content.html">select-object-content</a>
    *   <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/abort-multipart-upload.html">abort-multipart-upload</a>
    *   <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/complete-multipart-upload.html">complete-multipart-upload</a>
    *   <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/create-multipart-upload.html">create-multipart-upload</a>
    *   <li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/list-parts.html">list-parts</a> <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/upload-part.html">upload-part</a> <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/upload-part-copy.html">upload-part-copy</a> </ul>
    */
  val AwsS3Key: AttributeKey[String] =
    AttributeKey("aws.s3.key")

  /** The part number of the part being uploaded in a multipart-upload operation. This is a positive integer between 1
    * and 10,000.
    *
    * @note
    *   <p> The `part_number` attribute is only applicable to the <a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/upload-part.html">upload-part</a> and <a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/upload-part-copy.html">upload-part-copy</a>
    *   operations. The `part_number` attribute corresponds to the `--part-number` parameter of the <a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/upload-part.html">upload-part operation within the
    *   S3 API</a>.
    */
  val AwsS3PartNumber: AttributeKey[Long] =
    AttributeKey("aws.s3.part_number")

  /** Upload ID that identifies the multipart upload.
    *
    * @note
    *   <p> The `upload_id` attribute applies to S3 multipart-upload operations and corresponds to the `--upload-id`
    *   parameter of the <a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/index.html">S3 API</a>
    *   multipart operations. This applies in particular to the following operations: <ul> <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/abort-multipart-upload.html">abort-multipart-upload</a>
    *   <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/complete-multipart-upload.html">complete-multipart-upload</a>
    *   <li><a href="https://docs.aws.amazon.com/cli/latest/reference/s3api/list-parts.html">list-parts</a> <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/upload-part.html">upload-part</a> <li><a
    *   href="https://docs.aws.amazon.com/cli/latest/reference/s3api/upload-part-copy.html">upload-part-copy</a> </ul>
    */
  val AwsS3UploadId: AttributeKey[String] =
    AttributeKey("aws.s3.upload_id")

  /** The ARN of the Secret stored in the Secrets Mangger
    */
  val AwsSecretsmanagerSecretArn: AttributeKey[String] =
    AttributeKey("aws.secretsmanager.secret.arn")

  /** The ARN of the AWS SNS Topic. An Amazon SNS <a
    * href="https://docs.aws.amazon.com/sns/latest/dg/sns-create-topic.html">topic</a> is a logical access point that
    * acts as a communication channel.
    */
  val AwsSnsTopicArn: AttributeKey[String] =
    AttributeKey("aws.sns.topic.arn")

  /** The URL of the AWS SQS Queue. It's a unique identifier for a queue in Amazon Simple Queue Service (SQS) and is
    * used to access the queue and perform actions on it.
    */
  val AwsSqsQueueUrl: AttributeKey[String] =
    AttributeKey("aws.sqs.queue.url")

  /** The ARN of the AWS Step Functions Activity.
    */
  val AwsStepFunctionsActivityArn: AttributeKey[String] =
    AttributeKey("aws.step_functions.activity.arn")

  /** The ARN of the AWS Step Functions State Machine.
    */
  val AwsStepFunctionsStateMachineArn: AttributeKey[String] =
    AttributeKey("aws.step_functions.state_machine.arn")

  /** Values for [[AwsEcsLaunchtype]].
    */
  abstract class AwsEcsLaunchtypeValue(val value: String)
  object AwsEcsLaunchtypeValue {
    implicit val attributeFromAwsEcsLaunchtypeValue: Attribute.From[AwsEcsLaunchtypeValue, String] = _.value

    /** Amazon EC2
      */
    case object Ec2 extends AwsEcsLaunchtypeValue("ec2")

    /** Amazon Fargate
      */
    case object Fargate extends AwsEcsLaunchtypeValue("fargate")
  }

}
