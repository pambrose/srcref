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

package com.srcref.pages

import com.pambrose.srcref.pages.Common.WIDTH_VAL
import com.pambrose.srcref.pages.Common.hasValues
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class CommonTest :
  StringSpec(
    {
      "hasValues returns false for empty map" {
        emptyMap<String, String?>().hasValues() shouldBe false
      }

      "hasValues returns false for all-null values" {
        mapOf("a" to null, "b" to null).hasValues() shouldBe false
      }

      "hasValues returns false for all-blank values" {
        mapOf("a" to "", "b" to "  ").hasValues() shouldBe false
      }

      "hasValues returns true when at least one non-blank value" {
        mapOf("a" to "", "b" to "test").hasValues() shouldBe true
      }

      "WIDTH_VAL is 93" {
        WIDTH_VAL shouldBe "93"
      }
    },
  )
