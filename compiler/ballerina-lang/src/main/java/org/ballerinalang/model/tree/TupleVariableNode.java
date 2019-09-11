/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.model.tree;

import org.wso2.ballerinalang.compiler.tree.BLangVariable;

import java.util.List;

/**
 *
 * Represents a tuple variable node.
 * Example:
 *      [string, int, float] [s, i, f] = ["Foo", 12, 4.5];
 *      [[string, boolean], int] [[s, b], i] = expr;
 *
 * @since 0.985.0
 */
public interface TupleVariableNode extends VariableNode, AnnotatableNode, DocumentableNode, TopLevelNode {

    List<? extends VariableNode> getVariables();
    
    void addVariable(VariableNode node);

    BLangVariable getRestVariable();

}
