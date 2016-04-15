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
;(function()
{
	// CommonJS
	typeof(require) != 'undefined' ? SyntaxHighlighter = require('shCore').SyntaxHighlighter : null;

	function Brush()
	{
		var keywords =	'if fi then elif else for do done until while break continue case function return in eq ne ge le';
		var commands =  'alias apropos awk basename bash bc bg builtin bzip2 cal cat cd cfdisk chgrp chmod chown chroot' +
						'cksum clear cmp comm command cp cron crontab csplit cut date dc dd ddrescue declare df ' +
						'diff diff3 dig dir dircolors dirname dirs du echo egrep eject enable env ethtool eval ' +
						'exec exit expand export expr false fdformat fdisk fg fgrep file find fmt fold format ' +
						'free fsck ftp gawk getopts grep groups gzip hash head history hostname id ifconfig ' +
						'import install join kill less let ln local locate logname logout look lpc lpr lprint ' +
						'lprintd lprintq lprm ls lsof make man mkdir mkfifo mkisofs mknod more mount mtools ' +
						'mv netstat nice nl nohup nslookup open op passwd paste pathchk ping popd pr printcap ' +
						'printenv printf ps pushd pwd quota quotacheck quotactl ram rcp read readonly renice ' +
						'remsync rm rmdir rsync screen scp sdiff sed select seq set sftp shift shopt shutdown ' +
						'sleep sort source split ssh strace su sudo sum symlink sync tail tar tee test time ' +
						'times touch top traceroute trap tr true tsort tty type ulimit umask umount unalias ' +
						'uname unexpand uniq units unset unshar useradd usermod users uuencode uudecode v vdir ' +
						'vi watch wc whereis which who whoami Wget xargs yes'
						;

		this.regexList = [
			{ regex: /^#!.*$/gm,											css: 'preprocessor bold' },
			{ regex: /\/[\w-\/]+/gm,										css: 'plain' },
			{ regex: SyntaxHighlighter.regexLib.singleLinePerlComments,		css: 'comments' },		// one line comments
			{ regex: SyntaxHighlighter.regexLib.doubleQuotedString,			css: 'string' },		// double quoted strings
			{ regex: SyntaxHighlighter.regexLib.singleQuotedString,			css: 'string' },		// single quoted strings
			{ regex: new RegExp(this.getKeywords(keywords), 'gm'),			css: 'keyword' },		// keywords
			{ regex: new RegExp(this.getKeywords(commands), 'gm'),			css: 'functions' }		// commands
			];
	}

	Brush.prototype	= new SyntaxHighlighter.Highlighter();
	Brush.aliases	= ['bash', 'shell'];

	SyntaxHighlighter.brushes.Bash = Brush;

	// CommonJS
	typeof(exports) != 'undefined' ? exports.Brush = Brush : null;
})();
