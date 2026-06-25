package com.gradge.erp.finance.dto;

import com.gradge.erp.finance.entity.Expense;
import com.gradge.erp.finance.entity.Income;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-26T01:47:14+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class FinanceMapperImpl implements FinanceMapper {

    @Override
    public Expense toExpenseEntity(ExpenseRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        Expense.ExpenseBuilder expense = Expense.builder();

        expense.description( dto.getDescription() );
        expense.category( dto.getCategory() );
        expense.amount( dto.getAmount() );
        expense.date( dto.getDate() );
        expense.reference( dto.getReference() );

        return expense.build();
    }

    @Override
    public ExpenseResponseDto toExpenseResponseDto(Expense entity) {
        if ( entity == null ) {
            return null;
        }

        ExpenseResponseDto.ExpenseResponseDtoBuilder expenseResponseDto = ExpenseResponseDto.builder();

        expenseResponseDto.id( entity.getId() );
        expenseResponseDto.description( entity.getDescription() );
        expenseResponseDto.category( entity.getCategory() );
        expenseResponseDto.amount( entity.getAmount() );
        expenseResponseDto.date( entity.getDate() );
        expenseResponseDto.reference( entity.getReference() );
        expenseResponseDto.createdAt( entity.getCreatedAt() );
        expenseResponseDto.updatedAt( entity.getUpdatedAt() );

        return expenseResponseDto.build();
    }

    @Override
    public List<ExpenseResponseDto> toExpenseResponseDtoList(List<Expense> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ExpenseResponseDto> list = new ArrayList<ExpenseResponseDto>( entities.size() );
        for ( Expense expense : entities ) {
            list.add( toExpenseResponseDto( expense ) );
        }

        return list;
    }

    @Override
    public void updateExpenseFromDto(ExpenseRequestDto dto, Expense entity) {
        if ( dto == null ) {
            return;
        }

        entity.setDescription( dto.getDescription() );
        entity.setCategory( dto.getCategory() );
        entity.setAmount( dto.getAmount() );
        entity.setDate( dto.getDate() );
        entity.setReference( dto.getReference() );
    }

    @Override
    public Income toIncomeEntity(IncomeRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        Income.IncomeBuilder income = Income.builder();

        income.description( dto.getDescription() );
        income.category( dto.getCategory() );
        income.amount( dto.getAmount() );
        income.date( dto.getDate() );
        income.reference( dto.getReference() );

        return income.build();
    }

    @Override
    public IncomeResponseDto toIncomeResponseDto(Income entity) {
        if ( entity == null ) {
            return null;
        }

        IncomeResponseDto.IncomeResponseDtoBuilder incomeResponseDto = IncomeResponseDto.builder();

        incomeResponseDto.id( entity.getId() );
        incomeResponseDto.description( entity.getDescription() );
        incomeResponseDto.category( entity.getCategory() );
        incomeResponseDto.amount( entity.getAmount() );
        incomeResponseDto.date( entity.getDate() );
        incomeResponseDto.reference( entity.getReference() );
        incomeResponseDto.createdAt( entity.getCreatedAt() );
        incomeResponseDto.updatedAt( entity.getUpdatedAt() );

        return incomeResponseDto.build();
    }

    @Override
    public List<IncomeResponseDto> toIncomeResponseDtoList(List<Income> entities) {
        if ( entities == null ) {
            return null;
        }

        List<IncomeResponseDto> list = new ArrayList<IncomeResponseDto>( entities.size() );
        for ( Income income : entities ) {
            list.add( toIncomeResponseDto( income ) );
        }

        return list;
    }

    @Override
    public void updateIncomeFromDto(IncomeRequestDto dto, Income entity) {
        if ( dto == null ) {
            return;
        }

        entity.setDescription( dto.getDescription() );
        entity.setCategory( dto.getCategory() );
        entity.setAmount( dto.getAmount() );
        entity.setDate( dto.getDate() );
        entity.setReference( dto.getReference() );
    }
}
