package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.user.UserVO;
import greencity.entity.User;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.WrongIdException;
import greencity.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Negative scenario tests for UserServiceImpl getInitialsById method.
 * Tests null handling and edge cases.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplNegativeTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        // Any common setup if needed
    }

    @Test
    void getInitialsById_ShouldThrowWrongIdException_WhenUserIdIsNull() {
        // When & Then
        WrongIdException exception = assertThrows(WrongIdException.class,
            () -> userService.getInitialsById(null));
        
        assertEquals("User ID cannot be null", exception.getMessage());
    }

    @Test
    void getInitialsById_ShouldThrowWrongIdException_WhenUserNotFound() {
        // Given
        Long nonExistentUserId = 999L;
        when(userRepo.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then
        WrongIdException exception = assertThrows(WrongIdException.class,
            () -> userService.getInitialsById(nonExistentUserId));
        
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID + nonExistentUserId, exception.getMessage());
    }

    @Test
    void getInitialsById_ShouldThrowBadRequestException_WhenUserNameIsNull() {
        // Given
        Long userId = 1L;
        User user = new User();
        UserVO userVO = new UserVO();
        userVO.setName(null);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(modelMapper.map(any(User.class), eq(UserVO.class))).thenReturn(userVO);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> userService.getInitialsById(userId));
        
        assertEquals("User name is required for generating initials", exception.getMessage());
    }

    @Test
    void getInitialsById_ShouldThrowBadRequestException_WhenUserNameIsEmpty() {
        // Given
        Long userId = 1L;
        User user = new User();
        UserVO userVO = new UserVO();
        userVO.setName("   ");

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(modelMapper.map(any(User.class), eq(UserVO.class))).thenReturn(userVO);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> userService.getInitialsById(userId));
        
        assertEquals("User name is required for generating initials", exception.getMessage());
    }

    @Test
    void getInitialsById_ShouldReturnSingleInitial_WhenNameHasNoSpaces() {
        // Given
        Long userId = 1L;
        User user = new User();
        UserVO userVO = new UserVO();
        userVO.setName("John");

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(modelMapper.map(any(User.class), eq(UserVO.class))).thenReturn(userVO);

        // When
        String result = userService.getInitialsById(userId);

        // Then
        assertEquals("J", result);
    }

    @Test
    void getInitialsById_ShouldReturnTwoInitials_WhenNameHasSpaces() {
        // Given
        Long userId = 1L;
        User user = new User();
        UserVO userVO = new UserVO();
        userVO.setName("John Doe");

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(modelMapper.map(any(User.class), eq(UserVO.class))).thenReturn(userVO);

        // When
        String result = userService.getInitialsById(userId);

        // Then
        assertEquals("JD", result);
    }

    @Test
    void getInitialsById_ShouldTrimWhitespace_AndGenerateInitials() {
        // Given
        Long userId = 1L;
        User user = new User();
        UserVO userVO = new UserVO();
        userVO.setName("  John   Doe  ");

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(modelMapper.map(any(User.class), eq(UserVO.class))).thenReturn(userVO);

        // When
        String result = userService.getInitialsById(userId);

        // Then
        assertEquals("JD", result);
    }
}