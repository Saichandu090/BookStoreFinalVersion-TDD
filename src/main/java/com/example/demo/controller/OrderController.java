package com.example.demo.controller;

import com.example.demo.mapper.OrderMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.requestdto.OrderRequestDto;
import com.example.demo.responsedto.OrderResponseDto;
import com.example.demo.service.OrderService;
import com.example.demo.util.ResponseStructure;
import com.example.demo.util.Roles;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@AllArgsConstructor
public class OrderController
{
    private OrderService orderService;
    private UserMapper userMapper;
    private final OrderMapper orderMapper=new OrderMapper();
    private static final String HEADER="Authorization";

    @PostMapping("/placeOrder")
    public ResponseEntity<ResponseStructure<OrderResponseDto>> placeOrder(@RequestHeader(value = HEADER)String authHeader,@Valid @RequestBody OrderRequestDto orderRequestDto)
    {
        UserDetails userDetails=userMapper.validateUserToken(authHeader);
        if(userDetails!=null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return orderService.placeOrder(userDetails.getUsername(),orderRequestDto);
        }
        return orderMapper.noAuthority();
    }


    @DeleteMapping("/cancelOrder/{orderId}")
    public ResponseEntity<ResponseStructure<String>> cancelOrder(@RequestHeader(value = HEADER)String authHeader,@PathVariable Long orderId)
    {
        UserDetails userDetails=userMapper.validateUserToken(authHeader);
        if(userDetails!=null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return orderService.cancelOrder(userDetails.getUsername(),orderId);
        }
        return orderMapper.noAuthorityForMethod();
    }

    @GetMapping("/getOrder")
    public ResponseEntity<ResponseStructure<OrderResponseDto>> getOrder(@RequestHeader(value = HEADER)String authHeader,@RequestParam Long orderId)
    {
        UserDetails userDetails=userMapper.validateUserToken(authHeader);
        if(userDetails!=null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return orderService.getOrder(userDetails.getUsername(),orderId);
        }
        return orderMapper.noAuthority();
    }


    @GetMapping("/getAllOrders")
    public ResponseEntity<ResponseStructure<List<OrderResponseDto>>> getAllOrdersForUser(@RequestHeader(value = HEADER)String authHeader)
    {
        UserDetails userDetails=userMapper.validateUserToken(authHeader);
        if(userDetails!=null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return orderService.getAllOrdersForUser(userDetails.getUsername());
        }
        return orderMapper.unAuthorized();
    }
}
