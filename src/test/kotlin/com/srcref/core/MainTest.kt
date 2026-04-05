/*
 *   Copyright © 2026 Paul Ambrose (pambrose@mac.com)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.srcref.core

import com.pambrose.srcref.startsWithList
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MainTest :
  StringSpec(
    {
      "startsWithList returns true when prefix matches" {
        "/ping".startsWithList(listOf("/ping", "/pong")) shouldBe true
      }

      "startsWithList returns false when no match" {
        "/other".startsWithList(listOf("/ping", "/pong")) shouldBe false
      }

      "startsWithList returns false for empty list" {
        "/anything".startsWithList(emptyList()) shouldBe false
      }

      "startsWithList with empty string input" {
        "".startsWithList(listOf("/ping")) shouldBe false
      }

      "startsWithList with partial prefix match" {
        "/pin".startsWithList(listOf("/ping")) shouldBe false
      }
    },
  )
