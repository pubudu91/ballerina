/*
 *  Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.stdlib.task.utils;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.stdlib.task.exceptions.SchedulingException;
import org.ballerinalang.stdlib.task.objects.ServiceInformation;

import java.util.Objects;

import static org.ballerinalang.stdlib.task.utils.TaskConstants.DETAIL_RECORD_NAME;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_DAYS_OF_MONTH;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_DAYS_OF_WEEK;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_HOURS;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_MINUTES;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_MONTHS;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_SECONDS;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_YEAR;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.LISTENER_ERROR_REASON;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.RECORD_APPOINTMENT_DATA;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.RESOURCE_ON_TRIGGER;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.TASK_PACKAGE_ID;
import static org.quartz.CronExpression.isValidExpression;

/**
 * Utility functions used in ballerina task module.
 *
 * @since 0.995.0
 */
public class Utils {

    // valid resource count is set to to because the init function is also received as
    // an attached function.
    private static final int VALID_RESOURCE_COUNT = 1;

    public static ErrorValue createTaskError(String message) {
        return createTaskError(LISTENER_ERROR_REASON, message);
    }

    public static ErrorValue createTaskError(String reason, String message) {
        MapValue<String, Object> detail = createTaskDetailRecord(message);
        return BallerinaErrors.createError(reason, detail);
    }

    private static MapValue<String, Object> createTaskDetailRecord(String message) {
        return createTaskDetailRecord(message, null);
    }

    private static MapValue<String, Object> createTaskDetailRecord(String message, ErrorValue cause) {
        MapValue<String, Object> detail = BallerinaValues.createRecordValue(TASK_PACKAGE_ID, DETAIL_RECORD_NAME);
        return BallerinaValues.createRecord(detail, message, cause);
    }

    @SuppressWarnings("unchecked")
    public static String getCronExpressionFromAppointmentRecord(Object record) throws SchedulingException {
        String cronExpression;
        if (RECORD_APPOINTMENT_DATA.equals(TypeChecker.getType(record).getName())) {
            cronExpression = buildCronExpression((MapValue<String, Object>) record);
            if (!isValidExpression(cronExpression)) {
                throw new SchedulingException("AppointmentData \"" + record.toString() + "\" is invalid.");
            }
        } else {
            cronExpression = record.toString();
            if (!isValidExpression(cronExpression)) {
                throw new SchedulingException("Cron Expression \"" + cronExpression + "\" is invalid.");
            }
        }
        return cronExpression;
    }

    // Following code is reported as duplicates since all the lines doing same function call.
    private static String buildCronExpression(MapValue<String, Object> record) {
        String cronExpression = getStringFieldValue(record, FIELD_SECONDS) + " " +
                getStringFieldValue(record, FIELD_MINUTES) + " " +
                getStringFieldValue(record, FIELD_HOURS) + " " +
                getStringFieldValue(record, FIELD_DAYS_OF_MONTH) + " " +
                getStringFieldValue(record, FIELD_MONTHS) + " " +
                getStringFieldValue(record, FIELD_DAYS_OF_WEEK) + " " +
                getStringFieldValue(record, FIELD_YEAR);
        return cronExpression.trim();
    }

    private static String getStringFieldValue(MapValue<String, Object> record, String fieldName) {
        if (FIELD_DAYS_OF_MONTH.equals(fieldName) && Objects.isNull(record.get(FIELD_DAYS_OF_MONTH))) {
            return "?";
        } else if (Objects.nonNull(record.get(fieldName))) {
            return record.get(fieldName).toString();
        } else {
            return "*";
        }
    }

    /*
     * TODO: Runtime validation is done as compiler plugin does not work right now.
     *       When compiler plugins can be run for the resources without parameters, this will be redundant.
     *       Issue: https://github.com/ballerina-platform/ballerina-lang/issues/14148
     */
    public static void validateService(ServiceInformation serviceInformation) throws SchedulingException {
        AttachedFunction[] resources = serviceInformation.getService().getType().getAttachedFunctions();
        if (resources.length != VALID_RESOURCE_COUNT) {
            throw new SchedulingException(
                    "Invalid number of resources found in service \'" + serviceInformation.getServiceName()
                            + "\'. Task service should include only one resource.");
        }
        AttachedFunction resource = resources[0];

        if (RESOURCE_ON_TRIGGER.equals(resource.getName())) {
            validateOnTriggerResource(resource.getReturnParameterType());
        } else {
            throw new SchedulingException("Invalid resource function found: " + resource.getName()
                    + ". Expected: \'" + RESOURCE_ON_TRIGGER + "\'.");
        }
    }

    private static void validateOnTriggerResource(BType returnParameterType) throws SchedulingException {
        if (returnParameterType != org.ballerinalang.jvm.types.BTypes.typeNull) {
            throw new SchedulingException(
                    "Invalid resource function signature: \'" + RESOURCE_ON_TRIGGER + "\' should not return a value.");
        }
    }
}
