package com.benwanlh.movieservice.exceptions

import com.benwanlh.movieservice.enums.Role

class InvalidRoleException(val personName: String, val role: Role) :
    RuntimeException("$personName does not have the role $role")