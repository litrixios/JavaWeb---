// ReviewTrackingDTO.java
package com.bjfu.cms.entity.dto;
import lombok.Data;
import java.util.List;

@Data
public class ReviewTrackingDTO {
    private Integer manuscriptId; // 对应 ManuscriptID
    private String title;         // 对应 Title
    private String keywords;      // 对应 Keywords
    private String subStatus;     // 对应 SubStatus
    private List<ReviewOpinionDTO> opinions;
}