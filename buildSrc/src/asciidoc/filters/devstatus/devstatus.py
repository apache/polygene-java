#!/usr/bin/env python
# -*- mode: Python; coding: utf-8 -*-
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""
source=ignored
tag=self-test
"""

import sys
import xml.dom.minidom

PATH_PATTERN="%(source)s"
NS="http://polygene.apache.org/schemas/2008/dev-status/1"

def configuration(indata):
    config = {}
    for line in indata:
        line = line.strip()
        if not line: continue
        try:
            key, value = line.split('=',1)
        except:
            raise ValueError('invalid config line: "%s"' % (line,))
        config[key] = value
    return config

def devstatus(source=None,  **other):
    for key in other:
        sys.stderr.write("WARNING: unknown config key: '%s'\n" % key)
    if not source: raise ValueError("'source' must be specified")

    tablength = '    '
    sourceFile = open(PATH_PATTERN % locals())
    xmlDoc = xml.dom.minidom.parse(sourceFile)
    mindent = 1<<32 # a large number - no indentation is this long

    try:
        docs = xmlDoc.getElementsByTagNameNS(NS, "documentation")[0].childNodes[0].nodeValue
        code = xmlDoc.getElementsByTagNameNS(NS, "codebase")[0].childNodes[0].nodeValue
        tests = xmlDoc.getElementsByTagNameNS(NS, "unittests")[0].childNodes[0].nodeValue
        buff = []

        buff.append( "<para role=\"devstatus-code-"+code+"\">")
        buff.append( "code")
        buff.append( "</para>\n")

        buff.append( "<para role=\"devstatus-docs-"+docs+"\">")
        buff.append( "docs")
        buff.append( "</para>\n")

        buff.append( "<para role=\"devstatus-tests-"+tests+"\">")
        buff.append( "tests")
        buff.append( "</para>\n")

    finally:
        sourceFile.close()

    for line in buff:
        if line:
            yield line
        else:
            yield '\n'


if __name__ == '__main__':
    import traceback
    indata = sys.stdin
    if len(sys.argv) == 2 and sys.argv[1] == '--self-test':
        PATH_PATTERN = __file__
        indata = __doc__.split('\n')
    try:
        for line in devstatus(**configuration(indata)):
            sys.stdout.write(line)
    except:
        traceback.print_exc(file=sys.stdout)
        raise
