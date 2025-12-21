package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.ManuscriptMeta;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ManuscriptMetaMapper {

    @Insert("INSERT INTO ManuscriptMeta (ManuscriptID, Topic, RecommendedReviewers, CoverLetterContent) " +
            "VALUES (#{manuscriptId}, #{topic}, #{recommendedReviewers}, #{coverLetterContent})")
    void insert(ManuscriptMeta meta);

    @Update("UPDATE ManuscriptMeta SET Topic=#{topic}, RecommendedReviewers=#{recommendedReviewers}, " +
            "CoverLetterContent=#{coverLetterContent} WHERE ManuscriptID=#{manuscriptId}")
    void updateByManuscriptId(ManuscriptMeta meta);

    @Select("SELECT * FROM ManuscriptMeta WHERE ManuscriptID = #{manuscriptId}")
    ManuscriptMeta selectByManuscriptId(Integer manuscriptId);
}