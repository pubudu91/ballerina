// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

type Person1 abstract object {
    public int age = 10;
    public string name = "sample name";
};

type Employee1 object {
    public float salary = 0.0;
};

type Manager1 object {
    *Person1;

    string dpt = "HR";

    // refering a non-abstarct object
    *Employee1;
};

type EmployeeWithSalary abstract object {
    public float salary = 0.0;
};

type AnotherEmployeeWithSalary abstract object {
    public int salary = 0;
};

type ManagerWithTwoSalaries object {
    *Person1;

    string dpt = "HR";
    *EmployeeWithSalary;
    *AnotherEmployeeWithSalary;
};

// Direct circular reference
type Foo abstract object {
    *Foo;
};

// Indirect circular references
type A abstract object {
    *B;
};

type B abstract object {
    *C;
};

type C abstract object {
    *D;
    *E;
};

type D abstract object {
    *A;
};

type E abstract object {
    *C;
};

// Test errors for unimplemented methods
type Person2 abstract object {
    public int age = 10;
    public string name = "sample name";

    // Unimplemented function at the nested referenced type.
    public function getName(string? title) returns string;
};

type Employee2 abstract object {
    *Person2;
    public float salary = 0.0;

    // Unimplemented function at the referenced type.
    public function getSalary() returns float;
};

type Manager2 object {
    string dpt = "HR";
    *Employee2;
};

type P object {
    *Q;
};

type Q record {
    int x = 0;
    string y = "";
};

type R object {
    *Person1;
    *Person1;
};

type ObjectWithFunction abstract object {
    public function getName(string? title) returns string;
};

type ObjectWithRedeclaredFunction_1 abstract object {
    *ObjectWithFunction;
    public function getName(string? title) returns string;
};

type ObjectWithRedeclaredFunction_2 abstract object {
    *ObjectWithFunction;
    *ObjectWithRedeclaredFunction_1;
};

type RedecalredFieldObject_1 abstract object {
    int x = 0;
};

type RedecalredFieldObject_2 abstract object {
    int x = 0;
    *RedecalredFieldObject_1;
};

type RedecalredFieldObject_3 abstract object {
    *RedecalredFieldObject_2;
};

type Bar object {
    *Baz;   // non existing type
};

type Too object {
    *Tar; // non existing type

    string s; // with member

    public function __init(string s) {
        self.s = s;
    }
};
