package com.andreidodu.controller;

import com.andreidodu.dto.DeleteStatusDTO;
import com.andreidodu.dto.PaymentTypeDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.service.PaymentTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/paymentType")
@RequiredArgsConstructor
public class PaymentTypeController {

    final private PaymentTypeService paymentTypeService;

    @GetMapping("/{id}")
    public ResponseEntity<PaymentTypeDTO> get(@PathVariable Long id) throws ApplicationException {
        return ResponseEntity.ok(this.paymentTypeService.get(id));
    }

    @PostMapping
    public ResponseEntity<PaymentTypeDTO> save(@RequestBody PaymentTypeDTO paymentTypeDTO) throws ApplicationException {
        return ResponseEntity.ok(this.paymentTypeService.save(paymentTypeDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentTypeDTO> update(@PathVariable Long id, @RequestBody PaymentTypeDTO paymentTypeDTO) throws ApplicationException {
        return ResponseEntity.ok(this.paymentTypeService.update(id, paymentTypeDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteStatusDTO> delete(@PathVariable Long id) {
        this.paymentTypeService.delete(id);
        return ResponseEntity.ok(new DeleteStatusDTO(true));
    }
}
