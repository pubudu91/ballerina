/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.jvm.methodvisitor;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nativeimpl.jvm.ASMUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.objectweb.asm.MethodVisitor;

import static org.ballerinalang.model.types.TypeKind.ANY;
import static org.ballerinalang.model.types.TypeKind.OBJECT;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.JVM_PKG_PATH;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.METHOD_VISITOR;

/**
 * Native class for jvm method byte code creation.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "visitLdcInsn",
        receiver = @Receiver(type = OBJECT, structType = METHOD_VISITOR, structPackage = JVM_PKG_PATH),
        args = {
                @Argument(name = "value", type = ANY)
        }
)
public class VisitLdcInsn {

    public static void visitLdcInsn(Strand strand, ObjectValue oMv, Object oValue) {
        MethodVisitor mv = ASMUtil.getRefArgumentNativeData(oMv);
        if (Long.class.equals(oValue.getClass())) {
            long longVal = (Long) oValue;
            mv.visitLdcInsn(longVal);
        } else if (Double.class.equals(oValue.getClass())) {
            double doubleVal = (Double) oValue;
            mv.visitLdcInsn(doubleVal);
        } else if (String.class.equals(oValue.getClass())) {
            String stringVal = (String) oValue;
            mv.visitLdcInsn(stringVal);
        } else if (Boolean.class.equals(oValue.getClass())) {
            boolean booleanValue = (Boolean) oValue;
            mv.visitLdcInsn(booleanValue);
        } else if (Integer.class.equals(oValue.getClass())) {
            int intVal = (Integer) oValue;
            mv.visitLdcInsn(intVal);
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
