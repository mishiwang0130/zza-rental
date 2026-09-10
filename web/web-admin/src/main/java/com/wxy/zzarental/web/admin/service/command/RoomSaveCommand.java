package com.wxy.zzarental.web.admin.service.command;

import com.wxy.zzarental.model.enums.ReleaseStatus;
import com.wxy.zzarental.web.admin.service.dto.GraphDTO;
import java.util.List;
import lombok.Data;

@Data
public class RoomSaveCommand {

    private Long id;

    private String roomNumber;

    private java.math.BigDecimal rent;

    private Long apartmentId;

    private ReleaseStatus isRelease;

    private List<GraphDTO> graphVoList;

    private List<Long> attrValueIds;

    private List<Long> facilityInfoIds;

    private List<Long> labelInfoIds;

    private List<Long> paymentTypeIds;

    private List<Long> leaseTermIds;

}
