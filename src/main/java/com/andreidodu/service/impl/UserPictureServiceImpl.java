package com.andreidodu.service.impl;

import com.andreidodu.dto.UserPictureDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.mapper.UserPictureMapper;
import com.andreidodu.model.User;
import com.andreidodu.model.UserPicture;
import com.andreidodu.repository.UserPictureRepository;
import com.andreidodu.repository.UserRepository;
import com.andreidodu.service.UserPictureService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class UserPictureServiceImpl implements UserPictureService {

    private final UserPictureRepository userPictureRepository;
    private final UserRepository userRepository;

    private final UserPictureMapper userPictureMapper;

    @Override
    public UserPictureDTO get(Long id) throws ApplicationException {
        Optional<UserPicture> userPictureOptional = this.userPictureRepository.findById(id);
        validateUserPictureExistence(userPictureOptional);
        return this.userPictureMapper.toDTO(userPictureOptional.get());
    }

    private static void validateUserPictureExistence(Optional<UserPicture> userPictureOptional) throws ApplicationException {
        if (userPictureOptional.isEmpty()) {
            throw new ApplicationException("UserPicture not found");
        }
    }

    @Override
    public void delete(Long id) {
        this.userPictureRepository.deleteById(id);
    }

    @Override
    public UserPictureDTO save(UserPictureDTO userPictureDTO) throws ApplicationException {
        Optional<User> userOptional = userRepository.findById(userPictureDTO.getUserId());
        validateUserExistence(userOptional);
        UserPicture userPicture = this.userPictureMapper.toModel(userPictureDTO);
        userPicture.setUser(userOptional.get());
        final UserPicture userPictureSaved = this.userPictureRepository.save(userPicture);
        return this.userPictureMapper.toDTO(userPictureSaved);
    }

    private static void validateUserExistence(Optional<User> userOptional) throws ApplicationException {
        if (userOptional.isEmpty()) {
            throw new ApplicationException("user not found");
        }
    }

    @Override
    public UserPictureDTO update(Long id, UserPictureDTO userPictureDTO) throws ApplicationException {
        isUserPictureIdSame(id, userPictureDTO);
        Optional<UserPicture> userPictureOptional = this.userPictureRepository.findById(id);
        validateUserPictureExistence(userPictureOptional);
        UserPicture userPicture = userPictureOptional.get();
        this.userPictureMapper.getModelMapper().map(userPictureDTO, userPicture);
        UserPicture userSaved = this.userPictureRepository.save(userPicture);
        return this.userPictureMapper.toDTO(userSaved);

    }

    private static void isUserPictureIdSame(Long id, UserPictureDTO userPictureDTO) throws ApplicationException {
        if (!id.equals(userPictureDTO.getId())) {
            throw new ApplicationException("id not matching");
        }
    }

}
