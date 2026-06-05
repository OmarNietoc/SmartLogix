package com.smartlogix.order.mapper;

import com.smartlogix.order.model.Comuna;
import com.smartlogix.order.model.Order;
import com.smartlogix.order.model.OrderItem;
import com.smartlogix.order.model.OrderStatus;
import com.smartlogix.order.model.Region;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapperImpl();

    @Test
    void toDto_mapsOrderWithComunaRegionAndItems() {
        Order order = order(OrderStatus.PENDING);

        var dto = mapper.toDto(order);

        assertThat(dto.id()).isEqualTo("order-1");
        assertThat(dto.customerEmail()).isEqualTo("cliente@smartlogix.cl");
        assertThat(dto.comunaId()).isEqualTo(13123);
        assertThat(dto.comunaNombre()).isEqualTo("Santiago");
        assertThat(dto.regionNombre()).isEqualTo("Metropolitana");
        assertThat(dto.items()).hasSize(1);
        assertThat(dto.items().get(0).productName()).isEqualTo("Producto Demo");
        assertThat(dto.companyId()).isEqualTo("company-1");
    }

    @Test
    void itemToDto_mapsOrderItemFields() {
        OrderItem item = item();

        var dto = mapper.itemToDto(item);

        assertThat(dto.id()).isEqualTo("item-1");
        assertThat(dto.productId()).isEqualTo("product-1");
        assertThat(dto.warehouseId()).isEqualTo("warehouse-1");
        assertThat(dto.quantity()).isEqualTo(2);
        assertThat(dto.price()).isEqualByComparingTo("12990");
    }

    @Test
    void mapperReturnsNullForNullInput() {
        assertThat(mapper.toDto(null)).isNull();
        assertThat(mapper.itemToDto(null)).isNull();
    }

    @Test
    void toDto_handlesOrderWithoutNestedComunaOrItems() {
        Order order = Order.builder()
                .id("order-2")
                .customerName("Cliente")
                .customerEmail("cliente@smartlogix.cl")
                .street("Av. Demo")
                .status(OrderStatus.PENDING)
                .total(BigDecimal.TEN)
                .companyId("company-1")
                .items(null)
                .build();

        var dto = mapper.toDto(order);

        assertThat(dto.comunaId()).isNull();
        assertThat(dto.regionNombre()).isNull();
        assertThat(dto.items()).isNull();
    }

    private Order order(OrderStatus status) {
        Region region = Region.builder().id(13).nombre("Metropolitana").build();
        Comuna comuna = Comuna.builder().id(13123).nombre("Santiago").region(region).build();
        Order order = Order.builder()
                .id("order-1")
                .customerName("Cliente Demo")
                .customerEmail("cliente@smartlogix.cl")
                .street("Av. Demo 123")
                .comuna(comuna)
                .status(status)
                .total(BigDecimal.valueOf(25980))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .companyId("company-1")
                .build();
        OrderItem item = item();
        item.setOrder(order);
        order.setItems(List.of(item));
        return order;
    }

    private OrderItem item() {
        return OrderItem.builder()
                .id("item-1")
                .productId("product-1")
                .warehouseId("warehouse-1")
                .productName("Producto Demo")
                .quantity(2)
                .price(BigDecimal.valueOf(12990))
                .build();
    }
}
