package com.andreidodu.service.impl;

import com.andreidodu.dto.PaymentTypeDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.mapper.PaymentTypeMapper;
import com.andreidodu.model.PaymentType;
import com.andreidodu.model.User;
import com.andreidodu.repository.PaymentTypeRepository;
import com.andreidodu.repository.UserRepository;
import com.andreidodu.service.PaymentTypeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class PaymentTypeServiceImpl implements PaymentTypeService {

    private final PaymentTypeRepository paymentTypeRepository;
    private final PaymentTypeMapper paymentTypeMapper;
    private final UserRepository userRepository;

    @Override
    public PaymentTypeDTO get(Long id) throws ApplicationException {
        Optional<PaymentType> modelOpt = this.paymentTypeRepository.findById(id);
        validatePaymentTypeExistence(modelOpt);
        return this.paymentTypeMapper.toDTO(modelOpt.get());
    }

    private static void validatePaymentTypeExistence(Optional<PaymentType> modelOpt) throws ApplicationException {
        if (modelOpt.isEmpty()) {
            throw new ApplicationException("PaymentType not found");
        }
    }

    @Override
    public void delete(Long id) {
        this.paymentTypeRepository.deleteById(id);
    }

    @Override
    public PaymentTypeDTO save(PaymentTypeDTO paymentTypeDTO) throws ApplicationException {
        Optional<User> user = userRepository.findById(paymentTypeDTO.getUserId());
        validateUsetExistence(user);
        PaymentType model = this.paymentTypeMapper.toModel(paymentTypeDTO);
        model.setUser(user.get());
        final PaymentType paymentType = this.paymentTypeRepository.save(model);
        return this.paymentTypeMapper.toDTO(paymentType);
    }

    private static void validateUsetExistence(Optional<User> user) throws ApplicationException {
        if (user.isEmpty()) {
            throw new ApplicationException("user not found ");
        }
    }

    @Override
    public PaymentTypeDTO update(Long id, PaymentTypeDTO paymentTypeDTO) throws ApplicationException {
        validateUpdateInput(id, paymentTypeDTO);
        Optional<PaymentType> paymentTypeOptional = this.paymentTypeRepository.findById(id);
        validatePaymentTypeExistence(paymentTypeOptional);
        PaymentType paymentType = paymentTypeOptional.get();

        this.paymentTypeMapper.getModelMapper().map(paymentTypeDTO, paymentType);
        PaymentType userSaved = this.paymentTypeRepository.save(paymentType);
        return this.paymentTypeMapper.toDTO(userSaved);
    }

    private static void validateUpdateInput(Long id, PaymentTypeDTO paymentTypeDTO) throws ApplicationException {
        if (!isPaymentTypeIdSame(id, paymentTypeDTO)) {
            throw new ApplicationException("id not matching");
        }
    }

    private static boolean isPaymentTypeIdSame(Long id, PaymentTypeDTO paymentTypeDTO) {
        return id.equals(paymentTypeDTO.getId());
    }

}
