package cn.iocoder.yudao.module.swm.dal.dataobject;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDateTime;
@Data @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true) @TableName("swm_safety_person_training")
public class SwmSafetyPersonTrainingDO extends SwmBaseDO {
    private String safetyManageId;
    private String personId;
    private String personName;
    private String phoneNumber;
    private String identityCard;
    private String trainingType;
    private LocalDateTime trainingTime;
    private String trainingContent;
    private String trainingResult;
    private String completeStatus;
    private String progress;
}
