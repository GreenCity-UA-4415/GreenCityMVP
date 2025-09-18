package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.habitfact.HabitFactPostDto;
import greencity.dto.habitfact.HabitFactVO;
import greencity.dto.user.HabitIdRequestDto;
import greencity.entity.Habit;
import greencity.entity.HabitFact;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.HabitFactRepo;
import greencity.repository.HabitFactTranslationRepo;
import greencity.repository.HabitRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive negative scenario tests for HabitFactServiceImpl.
 * Tests edge cases, null handling, and error conditions.
 */
@ExtendWith(MockitoExtension.class)
class HabitFactServiceImplNegativeTest {

    @Mock
    private HabitFactRepo habitFactRepo;

    @Mock
    private HabitFactTranslationRepo habitFactTranslationRepo;

    @Mock
    private HabitRepo habitRepo;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private HabitFactServiceImpl habitFactService;

    private HabitFactPostDto invalidHabitFactPostDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
        invalidHabitFactPostDto = new HabitFactPostDto();
    }

    @Test
    void getAllHabitFacts_ShouldThrowBadRequestException_WhenPageableIsNull() {
        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> habitFactService.getAllHabitFacts(null, "en"));
        
        assertEquals("Pageable cannot be null", exception.getMessage());
    }

    @Test
    void getAllHabitFacts_ShouldThrowBadRequestException_WhenLanguageIsNull() {
        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> habitFactService.getAllHabitFacts(pageable, null));
        
        assertEquals("Language code cannot be null or empty", exception.getMessage());
    }

    @Test
    void getAllHabitFacts_ShouldThrowBadRequestException_WhenLanguageIsEmpty() {
        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> habitFactService.getAllHabitFacts(pageable, "   "));
        
        assertEquals("Language code cannot be null or empty", exception.getMessage());
    }

    @Test
    void save_ShouldThrowBadRequestException_WhenHabitFactPostDtoIsNull() {
        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> habitFactService.save(null));
        
        assertEquals("HabitFactPostDto and habit ID cannot be null", exception.getMessage());
    }

    @Test
    void save_ShouldThrowBadRequestException_WhenHabitIsNull() {
        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> habitFactService.save(invalidHabitFactPostDto));
        
        assertEquals("HabitFactPostDto and habit ID cannot be null", exception.getMessage());
    }

    @Test
    void save_ShouldThrowNotFoundException_WhenHabitDoesNotExist() {
        // Given
        invalidHabitFactPostDto.setHabit(createTestHabitIdRequestDto(1L));
        when(habitRepo.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> habitFactService.save(invalidHabitFactPostDto));
        
        assertEquals(ErrorMessage.HABIT_NOT_FOUND_BY_ID + 1L, exception.getMessage());
    }

    @Test
    void getHabitFactById_ShouldThrowNotFoundException_WhenHabitFactNotFound() {
        // Given
        Long nonExistentId = 999L;
        when(habitFactRepo.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> habitFactService.getHabitFactById(nonExistentId));
        
        assertEquals(ErrorMessage.HABIT_FACT_NOT_FOUND_BY_ID + nonExistentId, exception.getMessage());
    }

    @Test
    void delete_ShouldThrowNotDeletedException_WhenHabitFactNotFound() {
        // Given
        Long nonExistentId = 999L;
        when(habitFactRepo.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(Exception.class, () -> habitFactService.delete(nonExistentId));
    }

    @Test
    void save_ShouldHandleNullTranslations_Gracefully() {
        // Given
        invalidHabitFactPostDto.setHabit(createTestHabitIdRequestDto(1L));
        when(habitRepo.findById(1L)).thenReturn(Optional.of(new Habit()));
        
        HabitFact habitFactWithNullTranslations = new HabitFact();
        habitFactWithNullTranslations.setTranslations(null);
        
        when(modelMapper.map(any(HabitFactPostDto.class), eq(HabitFact.class)))
            .thenReturn(habitFactWithNullTranslations);
        when(habitFactRepo.save(any(HabitFact.class))).thenReturn(habitFactWithNullTranslations);
        when(modelMapper.map(any(HabitFact.class), eq(HabitFactVO.class)))
            .thenReturn(new HabitFactVO());

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> habitFactService.save(invalidHabitFactPostDto));
    }

    private HabitIdRequestDto createTestHabitIdRequestDto(Long id) {
        HabitIdRequestDto habitIdRequestDto = new HabitIdRequestDto();
        habitIdRequestDto.setId(id);
        return habitIdRequestDto;
    }
}