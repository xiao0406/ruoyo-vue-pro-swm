package cn.iocoder.yudao.module.swm.dal.dataobject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDateTime;
@Data @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true) @TableName("swm_safety_person_training")
public class SwmSafetyPersonTrainingDO extends SwmBaseDO {
    private String safetyManageId;
    private String phoneNumber;
    private String identityCard;
    private String completeStatus;
    private LocalDateTime completeDate;
    private String progress;

    /** Fields joined from the person, organization and safety video tables. */
    @TableField(exist = false) private String title;
    @TableField(exist = false) private java.time.LocalDate pushDate;
    @TableField(exist = false) private String jobType;
    @TableField(exist = false) private String type;
    @TableField(exist = false) private String personName;
    @TableField(exist = false) private String company;
    @TableField(exist = false) private String department;
    @TableField(exist = false) private String prodLine;
    @TableField(exist = false) private String team;
}
