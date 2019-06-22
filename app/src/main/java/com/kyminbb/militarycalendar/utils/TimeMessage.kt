package com.kyminbb.militarycalendar.utils

/**
 * declared top-level function
 * @param time represents time (related to clockwork activity)
 * returns string message according to time
 * 어떤 컨셉을 잡아놓고 메세지, 그리고 그 메세지에 대응되는 백그라운드 ui가 있으면 좋을듯
 */
fun showTimeMessage(time: Int) : String {
    when (time) {
        0, 1 -> return "잠에 들 시간입니다"
        2, 3 -> return "얕은 꿈이 당신을 뒤척이게 하네요"
        4, 5 -> return "귀뚜라미가 울고 있는 새벽이네요"
        6, 7 -> return "안개가 걷히고 은은한 아침햇살이 내립니다"
        8, 9 -> return "조금은 거센 바람이 당신을 괴롭힐수도 있겠네요"
        10, 11 -> return "이마에 맺힌 땀은 당신을 빛나게 합니다"
        12, 13 -> return "머리 위에 강렬한 태양이 떠있습니다"
        14, 15 -> return "나른한 오후, 조금은 따분한가요"
        16, 17 -> return "고된 그대도 모르게 눈꺼풀이 감깁니다"
        18, 19 -> return "곧 다가올 내일을 생각하며 새로운 준비를 합니다"
        20, 21 -> return "크게 떠오른 달이 당신을 설레게 하는군요"
        22, 23 -> return "마침내 끝을 알리는 아침이 다가오는 군요"
        else -> return ""
    }
}