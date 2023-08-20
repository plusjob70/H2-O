package com.h2o.h2oServer.domain.model_type.application;

import com.h2o.h2oServer.domain.car.exception.NoSuchCarException;
import com.h2o.h2oServer.domain.model_type.Entity.*;
import com.h2o.h2oServer.domain.model_type.dto.CarBodytypeDto;
import com.h2o.h2oServer.domain.model_type.dto.CarDrivetrainDto;
import com.h2o.h2oServer.domain.model_type.dto.CarPowertrainDto;
import com.h2o.h2oServer.domain.model_type.dto.ModelTypeDto;
import com.h2o.h2oServer.domain.model_type.mapper.BodytypeMapper;
import com.h2o.h2oServer.domain.model_type.mapper.DrivetrainMapper;
import com.h2o.h2oServer.domain.model_type.mapper.PowertrainMapper;
import com.h2o.h2oServer.domain.model_type.mapper.TechnicalSpecMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Stream;

import static com.h2o.h2oServer.domain.model_type.DrivetrainFixture.*;
import static com.h2o.h2oServer.domain.model_type.PowertrainFixture.*;
import static com.h2o.h2oServer.domain.model_type.BodyTypeFixture.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class ModelTypeServiceTest {
    private ModelTypeService modelTypeService;
    private PowertrainMapper powerTrainMapper;
    private BodytypeMapper bodyTypeMapper;
    private DrivetrainMapper driveTrainMapper;
    private TechnicalSpecMapper technicalSpecMapper;
    private SoftAssertions softly;

    @BeforeEach
    void setup() {
        powerTrainMapper = Mockito.mock(PowertrainMapper.class);
        bodyTypeMapper = Mockito.mock(BodytypeMapper.class);
        driveTrainMapper = Mockito.mock(DrivetrainMapper.class);
        technicalSpecMapper = Mockito.mock(TechnicalSpecMapper.class);
        modelTypeService = new ModelTypeService(powerTrainMapper,
                bodyTypeMapper,
                driveTrainMapper,
                technicalSpecMapper);
        softly = new SoftAssertions();
    }

    @Test
    @DisplayName("존재하는 차량에 대해서 ModelTypeDto를 반환한다.")
    void findModelTypes() {
        //given
        Long carId = 1L;
        when(powerTrainMapper.findPowertrainsByCarId(carId)).thenReturn(generateCarPowerTrainEntities());
        when(powerTrainMapper.findOutput(anyLong())).thenReturn(generatePowertrainOutputEntity());
        when(powerTrainMapper.findTorque(anyLong())).thenReturn(generatePowertrainTorqueEntity());
        when(bodyTypeMapper.findBodytypesByCarId(carId)).thenReturn(generateCarBodyTypeEntities());
        when(driveTrainMapper.findDrivetrainsByCarId(carId)).thenReturn(generateCarDrivetrainEntities());

        //when
        ModelTypeDto modelTypeDto = modelTypeService.findModelTypes(carId);
        CarPowertrainDto carPowertrainDto = modelTypeDto.getPowertrains().get(0);
        CarBodytypeDto carBodytypeDto = modelTypeDto.getBodytypes().get(0);
        CarDrivetrainDto carDrivetrainDto = modelTypeDto.getDrivetrains().get(0);

        //then
        softly.assertThat(carPowertrainDto.getName()).as("파워트레인 이름은 powertrain1이다.").isEqualTo("powertrain1");
        softly.assertThat(carPowertrainDto.getImage()).as("파워트레인 이미지는 img_url1이다.").isEqualTo("img_url1");
        softly.assertThat(carBodytypeDto.getName()).as("바디타입 이름은 name1이다.").isEqualTo("name1");
        softly.assertThat(carBodytypeDto.getDescription()).as("바디타입 세부 설명은 description1이다.").isEqualTo("description1");
        softly.assertThat(carDrivetrainDto.getName()).as("구동방식 이름은 name 1이다.").isEqualTo("name1");
        softly.assertThat(carDrivetrainDto.getDescription()).as("구동방식 세부 설명은 description1이다.").isEqualTo("description1");
    }

    @ParameterizedTest
    @DisplayName("존재하지 않는 차량에 대해서 NoSuchCarException을 발생시킨다.")
    @MethodSource("provideInvalidResultsOfMappers")
    void findModelTypesNotExists(List<CarPowerTrainEntity> carPowerTrainEntities,
                                 PowertrainOutputEntity powertrainOutputEntity,
                                 PowertrainTorqueEntity powertrainTorqueEntity,
                                 List<CarBodytypeEntity> carBodytypeEntities,
                                 List<CarDrivetrainEntity> carDrivetrainEntities) {
        //given
        Long carId = 1L;
        when(powerTrainMapper.findPowertrainsByCarId(carId)).thenReturn(carPowerTrainEntities);
        when(powerTrainMapper.findOutput(anyLong())).thenReturn(powertrainOutputEntity);
        when(powerTrainMapper.findTorque(anyLong())).thenReturn(powertrainTorqueEntity);
        when(bodyTypeMapper.findBodytypesByCarId(carId)).thenReturn(carBodytypeEntities);
        when(driveTrainMapper.findDrivetrainsByCarId(carId)).thenReturn(carDrivetrainEntities);

        //when
        //then
        assertThatThrownBy(() -> modelTypeService.findModelTypes(carId))
                .isInstanceOf(NoSuchCarException.class);
    }

    public static Stream<Arguments> provideInvalidResultsOfMappers() {
        return Stream.of(
                Arguments.of(
                        List.of(),
                        generatePowertrainOutputEntity(),
                        generatePowertrainTorqueEntity(),
                        generateCarBodyTypeEntities(),
                        generateCarDrivetrainEntities()
                ),
                Arguments.of(
                        generateCarPowerTrainEntities(),
                        null,
                        generatePowertrainTorqueEntity(),
                        generateCarBodyTypeEntities(),
                        generateCarDrivetrainEntities()
                ),
                Arguments.of(
                        generateCarPowerTrainEntities(),
                        generatePowertrainOutputEntity(),
                        null,
                        generateCarBodyTypeEntities(),
                        generateCarDrivetrainEntities()
                ),
                Arguments.of(
                        generateCarPowerTrainEntities(),
                        generatePowertrainOutputEntity(),
                        generatePowertrainTorqueEntity(),
                        List.of(),
                        generateCarDrivetrainEntities()
                ),
                Arguments.of(
                        generateCarPowerTrainEntities(),
                        generatePowertrainOutputEntity(),
                        generatePowertrainTorqueEntity(),
                        generateCarBodyTypeEntities(),
                        List.of()
                )
        );
    }

    @Test
    void findTechnicalSpec() {
    }

    @Test
    void findDefaultModelType() {
    }
}
