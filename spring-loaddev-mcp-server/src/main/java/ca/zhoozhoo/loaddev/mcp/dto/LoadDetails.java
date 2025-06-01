package ca.zhoozhoo.loaddev.mcp.dto;

import java.util.List;

public record LoadDetails(

        LoadDto load,

        RifleDto rifle,

        List<GroupDto> groups) {
}
