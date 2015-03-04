#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#
# Git patch-file maker.
#

echo 'Usage: git-format-patch.sh <BRANCH_WITH_PATCH>'
echo 

PATCHED_BRANCH=$1
MODE=$2

if [ "${GG_HOME}" = "" ];
    then GG_HOME=$PWD
fi

. ${GG_HOME}/scripts/git-patch-prop.sh # Import properties.
. ${GG_HOME}/scripts/git-patch-prop-local.sh # Import user properties (it will rewrite global properties).


# Define functions.
formatPatch () {
    GIT_HOME=$1
    DEFAULT_BRANCH=$2
    PATCHED_BRANCH=$3
    PATCH_SUFFIX=$4

    echo
    echo '>>> FORMAT PATCH FOR BRANCHES '${DEFAULT_BRANCH}' AND '${PATCHED_BRANCH}' AT '${GIT_HOME}
    echo

    cd ${GIT_HOME}

#    echo '>>>>>> Checkout '${DEFAULT_BRANCH}
#    git checkout ${DEFAULT_BRANCH}

#    echo '>>>>>> Create tmpsquash.'
#    git checkout -b tmpsquash
#
#    echo '>>>>>> Merge '${PATCHED_BRANCH}' at tmpsquash'
#    git merge --squash ${PATCHED_BRANCH}
#    git commit -a -m "My squashed commits"

    git checkout ${PATCHED_BRANCH}

    echo '>>>>>> Format patch.'
    git format-patch ${DEFAULT_BRANCH}  --stdout > ${PATCHES_HOME}'/'${DEFAULT_BRANCH}_${PATCHED_BRANCH}${PATCH_SUFFIX}

#    echo '>>>>>> Clean-up.'
    git checkout ${DEFAULT_BRANCH}
#    git branch -D tmpsquash # Delete tmp branch.
}

updateBranches () {
    GIT_HOME=$1
    DEFAULT_BRANCH=$2
    PATCHED_BRANCH=$3

    echo
    echo '>>> UPDATING BRANCHES '${DEFAULT_BRANCH}' AND '${PATCHED_BRANCH}' AT '${GIT_HOME}
    echo

    cd ${GIT_HOME}

    git checkout ${DEFAULT_BRANCH}
    git pull

    git checkout ${PATCHED_BRANCH}
    echo
    echo '>>>>>> START MERGING'
    echo
    git merge --no-edit ${DEFAULT_BRANCH} # Merge with default message.
}

# Return value of checkBranchExists function.
BRANCH_EXISTS=''

checkBranchExists () {
    GIT_HOME=$1
    BRANCH=$2

    cd ${GIT_HOME}

    BRANCH_EXISTS=`git show-ref refs/heads/"${BRANCH}"`
}


exitIfBranchDoesNotExist () {
    checkBranchExists $1 $2

    if [ -z "$BRANCH_EXISTS" ] # If not.
    then
        echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
        echo ">>>>>>>>>>>>>>>>>>>>>>>>>> ERROR >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
        echo ">>> Expected branch ${BRANCH} does not exist at ${GIT_HOME}"
        echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
        
        exit
    fi
}

# Main script logic.

# Ignite project.
checkBranchExists ${IGNITE_HOME} ${PATCHED_BRANCH}

if [ -n "$BRANCH_EXISTS" ]
then
    exitIfBranchDoesNotExist ${IGNITE_HOME} ${IGNITE_DEFAULT_BRANCH}

    updateBranches ${IGNITE_HOME} ${IGNITE_DEFAULT_BRANCH} ${PATCHED_BRANCH}
    formatPatch ${IGNITE_HOME} ${IGNITE_DEFAULT_BRANCH} ${PATCHED_BRANCH} _ignite.patch
fi

# GridGain project.
checkBranchExists ${GG_HOME} ${PATCHED_BRANCH}

if [ -n "$BRANCH_EXISTS" ]
then
    exitIfBranchDoesNotExist ${GG_HOME} ${GG_DEFAULT_BRANCH}

    updateBranches ${GG_HOME} ${GG_DEFAULT_BRANCH} ${PATCHED_BRANCH}
    formatPatch ${GG_HOME} ${GG_DEFAULT_BRANCH} ${PATCHED_BRANCH} _gg.patch
fi