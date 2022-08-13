/*
 * Copyright 2022 Team GALAXIUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.galaxius.mnrega.data.remote.model.response2

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthResponse (

    @Json(name = "status" ) var status : State,
    @Json(name = "errors" ) var errors : Errors? = Errors(),
    @Json(name = "token"  ) var token  : String? = null,
    @Json(name = "user"   ) var user   : User?   = User()

)
enum class State {
    SUCCESS, NOT_FOUND, FAILED, UNAUTHORIZED
}


data class Errors (

    @Json(name = "msg" ) var msg : String? = null

)

data class User (

    @Json(name = "_id"      ) var Id       : String?  = null,
    @Json(name = "name"     ) var name     : String?  = null,
    @Json(name = "email"    ) var email    : String?  = null,
    @Json(name = "role"     ) var role     : String?  = null,
    @Json(name = "verified" ) var verified : Boolean? = null

)
