package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Employee;
import com.example.springboot.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/employee")
@Tag(name = "员工管理")
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;

    @GetMapping("/selectAll")
    @Operation(summary = "查询所有员工")
    public Result selectAll() {

        List<Employee> list = employeeService.selectAll();

        return Result.success(list);
    }

    @GetMapping("/selectById/{id}")
    @Operation(summary = "根据ID查询员工")
    public Result selectById(@PathVariable Integer id) {

        Employee employee = employeeService.selectById(id);
        return Result.success(employee);
    }

}
