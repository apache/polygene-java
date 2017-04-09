/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

var counter = 0;

function do1(message) {
    return "[" + message + "]";
}

//noinspection JSUnusedGlobalSymbols
function count() {
    return counter;
}

function inc() {
    return counter++;
}

function whatIsThis() {
    return This;
}

function whatIsState() {
    return state;
}

function whatIsApplication() {
    return application;
}

function whatIsLayer() {
    return layer;
}

function whatIsModule() {
    return module;
}

function whatIsObjectFactory() {
    return objectFactory;
}

function whatIsUnitOfWorkFactory() {
    return unitOfWorkFactory;
}

function whatIsValueBuilderFactory() {
    return valueBuilderFactory;
}

function whatIsTransientBuilderFactory() {
    return transientBuilderFactory;
}

function whatIsServiceFinder() {
    return serviceFinder;
}

function whatIsTypeLookup() {
    return typeLookup;
}