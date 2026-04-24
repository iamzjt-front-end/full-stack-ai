import type { NoteBrowseCountDto } from "./note-browse-count-dto";
import type { NoteBrowseTimeDto } from "./note-browse-time-dto";

export interface AdminDashboardDto { 
  userCount: number;
  noteCount: number;
  commentCount: number;
  noteBrowseCountDtoList: Array<NoteBrowseCountDto>;
  noteBrowseTimeDtoList: Array<NoteBrowseTimeDto>;
}