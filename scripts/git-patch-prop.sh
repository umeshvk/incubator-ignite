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
# Git patch-file maker/applyer properties.
#

IGNITE_DEFAULT_BRANCH='sprint-2'
GG_DEFAULT_BRANCH='ignite-sprint-2'

if [ "${IGNITE_HOME}" = "" ];
    then IGNITE_HOME=${GG_HOME}/../incubator-ignite
fi

PATCHES_HOME=${GG_HOME}