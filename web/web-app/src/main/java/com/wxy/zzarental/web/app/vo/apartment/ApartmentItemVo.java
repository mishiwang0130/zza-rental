package com.wxy.zzarental.web.app.vo.apartment;


import com.wxy.zzarental.model.entity.ApartmentInfo;
import com.wxy.zzarental.model.entity.LabelInfo;
import com.wxy.zzarental.web.app.vo.graph.GraphVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "App端公寓信息")
public class ApartmentItemVo extends ApartmentInfo {

    private List<LabelInfo> labelInfoList;

    private List<GraphVo> graphVoList;

    private BigDecimal minRent;
}
