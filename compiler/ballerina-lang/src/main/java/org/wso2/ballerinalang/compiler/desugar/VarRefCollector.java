/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.desugar;

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Takes an expression and recursively visits the expression and collects variable references in that expression.
 *
 * @since 1.1.1
 */
public class VarRefCollector extends BLangNodeVisitor {

    private static final CompilerContext.Key<VarRefCollector> VAR_REF_COLLECTOR_KEY = new CompilerContext.Key<>();

    private Set<BSymbol> varRefs;

    private VarRefCollector() {
    }

    public static VarRefCollector getInstance(CompilerContext context) {
        VarRefCollector varRefCollector = context.get(VAR_REF_COLLECTOR_KEY);
        if (varRefCollector == null) {
            varRefCollector = new VarRefCollector();
            context.put(VAR_REF_COLLECTOR_KEY, varRefCollector);
        }
        return varRefCollector;
    }

    public Set<BSymbol> collect(BLangExpression expr) {
        this.varRefs = new HashSet<>();
        collectVarRefs(expr);
        return this.varRefs;
    }

    private void collectVarRefs(BLangExpression expr) {
        if (expr == null) {
            return;
        }

        expr.accept(this);
    }

    @Override
    public void visit(BLangSimpleVarRef simpleVarRef) {
        if (simpleVarRef.symbol != null) {
            this.varRefs.add(simpleVarRef.symbol);
        }
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        collectVarRefs(binaryExpr.lhsExpr);
        collectVarRefs(binaryExpr.rhsExpr);
    }

    @Override
    public void visit(BLangInvocation invExpr) {
        collectVarRefs(invExpr.expr);

        for (BLangExpression argExpr : invExpr.argExprs) {
            collectVarRefs(argExpr);
        }

        for (BLangExpression requiredArg : invExpr.requiredArgs) {
            collectVarRefs(requiredArg);
        }

        for (BLangExpression restArg : invExpr.restArgs) {
            collectVarRefs(restArg);
        }
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        collectVarRefs(annotAccessExpr.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexBasedAccessExpr) {
        collectVarRefs(indexBasedAccessExpr.expr);
        collectVarRefs(indexBasedAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldBasedAccessExpr) {
        collectVarRefs(fieldBasedAccessExpr.expr);
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        collectVarRefs(xmlAttributeAccessExpr.expr);
        collectVarRefs(xmlAttributeAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        for (BLangExpression expr : listConstructorExpr.exprs) {
            collectVarRefs(expr);
        }
    }

    @Override
    public void visit(BLangArrowFunction arrowFunctionExpr) {
        collectVarRefs(arrowFunctionExpr);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        collectVarRefs(checkedExpr);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        collectVarRefs(checkPanickedExpr.expr);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        collectVarRefs(elvisExpr.lhsExpr);
        collectVarRefs(elvisExpr.rhsExpr);
    }

    @Override
    public void visit(BLangErrorVarRef errorVarRef) {
        for (BLangNamedArgsExpression argExpr : errorVarRef.detail) {
            collectVarRefs(argExpr);
        }
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        collectVarRefs(groupExpr.expression);
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
        // Nothing to do
    }

    @Override
    public void visit(BLangLiteral literal) {
        // Nothing to do
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : recordLiteral.keyValuePairs) {
            collectVarRefs(keyValuePair.key.expr);
            collectVarRefs(keyValuePair.valueExpr);
        }
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpr) {
        collectVarRefs(intRangeExpr.startExpr);
        collectVarRefs(intRangeExpr.endExpr);
    }

    @Override
    public void visit(BLangLambdaFunction lambdaFunction) {
        for (BLangSimpleVariable requiredParam : lambdaFunction.function.requiredParams) {
            collectVarRefs(requiredParam.expr);
        }

        if (lambdaFunction.function.restParam != null) {
            collectVarRefs(lambdaFunction.function.restParam.expr);
        }

        for (BLangWorker worker : lambdaFunction.function.workers) {
            for (BLangSimpleVariable requiredParam : worker.requiredParams) {
                collectVarRefs(requiredParam.expr);
            }

            if (worker.restParam != null) {
                collectVarRefs(worker.restParam.expr);
            }
        }

        // TODO: See if we need to consider the return type node
    }

    @Override
    public void visit(BLangNamedArgsExpression namedArgExpr) {
        collectVarRefs(namedArgExpr.expr);
    }

    @Override
    public void visit(BLangRecordVarRef recordVarRef) {
        // TODO: add this
    }

    @Override
    public void visit(BLangRestArgsExpression restArgsExpr) {
        collectVarRefs(restArgsExpr.expr);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        // TODO: Check if this needs to be implemented
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        for (BLangExpression expr : stringTemplateLiteral.exprs) {
            collectVarRefs(expr);
        }
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        collectVarRefs(tableLiteral.indexColumnsArrayLiteral);
        collectVarRefs(tableLiteral.keyColumnsArrayLiteral);

        for (BLangExpression tableDataRow : tableLiteral.tableDataRows) {
            collectVarRefs(tableDataRow);
        }
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpr) {
        // Do nothing
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        collectVarRefs(ternaryExpr.expr);
        collectVarRefs(ternaryExpr.thenExpr);
        collectVarRefs(ternaryExpr.elseExpr);
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        collectVarRefs(trapExpr.expr);
    }

    @Override
    public void visit(BLangTupleVarRef tupleVarRef) {
        for (BLangExpression expr : tupleVarRef.expressions) {
            collectVarRefs(expr);
        }
    }

    @Override
    public void visit(BLangTypeConversionExpr typeConversionExpr) {
        collectVarRefs(typeConversionExpr.expr);

        for (BLangAnnotationAttachment annAttachment : typeConversionExpr.annAttachments) {
            collectVarRefs(annAttachment.expr);
        }
    }

    @Override
    public void visit(BLangTypeInit typeInitExpr) {
        collectVarRefs(typeInitExpr.initInvocation);

        for (BLangExpression expr : typeInitExpr.argsExpr) {
            collectVarRefs(expr);
        }
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        collectVarRefs(typeTestExpr.expr);
    }

    @Override
    public void visit(BLangTypedescExpr typedescExpr) {
        // Do nothing
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        collectVarRefs(unaryExpr.expr);
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        collectVarRefs(xmlAttribute.name);
    }
}
