package com.gradge.erp.finance.dto;

import com.gradge.erp.finance.entity.Expense;
import com.gradge.erp.finance.entity.Income;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FinanceMapper {

    Expense toExpenseEntity(ExpenseRequestDto dto);

    ExpenseResponseDto toExpenseResponseDto(Expense entity);

    List<ExpenseResponseDto> toExpenseResponseDtoList(List<Expense> entities);

    void updateExpenseFromDto(ExpenseRequestDto dto, @MappingTarget Expense entity);

    Income toIncomeEntity(IncomeRequestDto dto);

    IncomeResponseDto toIncomeResponseDto(Income entity);

    List<IncomeResponseDto> toIncomeResponseDtoList(List<Income> entities);

    void updateIncomeFromDto(IncomeRequestDto dto, @MappingTarget Income entity);
}
