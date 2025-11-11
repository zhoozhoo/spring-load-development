package ca.zhoozhoo.loaddev.mcp.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.GRAM;
import static tech.units.indriya.unit.Units.METRE;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.zhoozhoo.loaddev.mcp.dto.LoadDto;

@SpringBootTest
class LoadDtoSerializationTest {

    @Autowired
    ObjectMapper mapper;

    @Test
    void writesLoadDtoWithQuantities() throws Exception {
        var json = mapper.writeValueAsString(new LoadDto(1L, "Test Load 1", "Test description",
                "Hodgdon", "H4350", "Hornady", "ELD-M",
                getQuantity(new BigDecimal("140"), GRAM), "CCI", "BR2",
                getQuantity(new BigDecimal("0.02"), METRE), getQuantity(new BigDecimal("0.071"), METRE),
                getQuantity(new BigDecimal("0.002"), METRE), 1L));
                
        assertThat(json).contains("\"bulletWeight\":{");
        assertThat(json).contains("\"value\":140");
        assertThat(json).contains("\"unit\":\"g\"");
    }
}
