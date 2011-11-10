#!/bin/sh

git clone git://github.com/Qi4j/qi4j-sdk.git
cd qi4j-sdk
git rm .gitmodules
git commit -a -m "Removing submodules and merging qi4j-core, qi4j-libraries and qi4j-extensions via subtree-merge"

git remote add -f core git@github.com:Qi4j/qi4j-core.git
git merge -s ours --no-commit core/develop
git read-tree --prefix=core/ -u core/develop
git commit -m "Merge 'qi4j-core' as into SDK"
git pull -s subtree core develop

git remote add -f libraries git@github.com:Qi4j/qi4j-libraries.git
git merge -s ours --no-commit libraries/develop
git read-tree --prefix=libraries/ -u libraries/develop
git commit -m "Merge 'qi4j-libraries' as into SDK"
git pull -s subtree libraries develop

git remote add -f extensions git@github.com:Qi4j/qi4j-extensions.git
git merge -s ours --no-commit extensions/develop
git read-tree --prefix=extensions/ -u extensions/develop
git commit -m "Merge 'qi4j-extensions' as into SDK"
git pull -s subtree extensions develop

