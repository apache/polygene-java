#!/usr/bin/env python
# -*- mode: Python; coding: utf-8 -*-
"""
source=ignored
tag=self-test
"""

import sys
import xml.dom.minidom

PATH_PATTERN="%(source)s"
NS="http://www.qi4j.org/schemas/2008/dev-status/1"

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
