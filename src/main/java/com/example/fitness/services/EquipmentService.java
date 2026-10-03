package com.example.fitness.services;

import com.example.fitness.dto.EquipmentDto;
import com.example.fitness.entitties.Equipment;
import com.example.fitness.repositories.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentDto create(String name, Integer capacity) {
        Equipment equipment = new Equipment();
        equipment.setName(name);
        equipment.setCapacity(capacity);
        equipmentRepository.save(equipment);
        return toDto(equipment);
    }

    public List<EquipmentDto> listAll() {
        return StreamSupport.stream(equipmentRepository.findAll().spliterator(), false)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void delete(Integer equipmentId) {
        equipmentRepository.deleteById(equipmentId);
    }

    private EquipmentDto toDto(Equipment equipment) {
        EquipmentDto dto = new EquipmentDto();
        dto.setEquipmentId(equipment.getEquipment_id());
        dto.setName(equipment.getName());
        dto.setCapacity(equipment.getCapacity());
        return dto;
    }
}