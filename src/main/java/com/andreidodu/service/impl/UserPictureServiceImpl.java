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
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class UserPictureServiceImpl implements UserPictureService {

    private final UserPictureRepository userPictureRepository;
    private final UserRepository userRepository;

    private final UserPictureMapper userPictureMapper;

    final static Supplier<ApplicationException> supplyUserPictureNotFoundException = () -> new ApplicationException("userPicture not found");

    @Override
    public UserPictureDTO get(Long userPictureId) throws ApplicationException {
        UserPicture userPicture = retrieveUserPicture(userPictureId)
                .orElseThrow(supplyUserPictureNotFoundException);

        return this.userPictureMapper.toDTO(userPicture);
    }

    private Optional<UserPicture> retrieveUserPicture(Long userPictureId) {
        return userPictureRepository.findById(userPictureId);
    }

    @Override
    public void delete(Long id) {
        this.userPictureRepository.deleteById(id);
    }

    @Override
    public UserPictureDTO save(UserPictureDTO userPictureDTO) throws ApplicationException {
        User user = userRepository.findById(userPictureDTO.getUserId())
                .orElseThrow(() -> new ApplicationException("user not found"));

        UserPicture userPicture = this.userPictureMapper.toModel(userPictureDTO);
        userPicture.setUser(user);

        final UserPicture userPictureSaved = this.userPictureRepository.save(userPicture);
        return this.userPictureMapper.toDTO(userPictureSaved);
    }

    @Override
    public UserPictureDTO update(Long id, UserPictureDTO userPictureDTO) throws ApplicationException {
        isUserPictureIdSame(id, userPictureDTO);
        UserPicture userPicture = this.userPictureRepository.findById(id)
                .orElseThrow(supplyUserPictureNotFoundException);
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
