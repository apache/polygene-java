#!/bin/sh
# git-create-remote-branch <branch_name>

if [ $# -ne 1 ]; then
         echo 1>&2 Usage: $0 branch_name
         exit 127
fi
 
export branch_name=$1
git push origin origin:refs/heads/${branch_name}
git fetch origin
git checkout --track -b ${branch_name} origin/${branch_name}
git pull
