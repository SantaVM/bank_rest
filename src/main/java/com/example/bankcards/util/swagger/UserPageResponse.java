package com.example.bankcards.util.swagger;

import com.example.bankcards.dto.UserListDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserPageResponse")
public class UserPageResponse extends PageResponse<UserListDto> {
}
