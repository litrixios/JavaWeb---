// ReviewOpinionDTO.java
package com.bjfu.cms.entity.dto;
import lombok.Data;

@Data
public class ReviewOpinionDTO {
    private Integer reviewId;
    private String reviewerName;
    private Double score;
    private String suggestion;
    private String confidentialComments;
    private String status;
}