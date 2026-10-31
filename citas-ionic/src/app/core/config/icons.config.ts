import { addIcons } from 'ionicons';
import {
  alertCircleOutline,
  arrowBackOutline,
  arrowForwardOutline,
  calendarOutline,
  callOutline,
  checkmarkCircleOutline,
  documentTextOutline,
  eyeOffOutline,
  eyeOutline,
  heartOutline,
  informationCircleOutline,
  lockClosedOutline,
  logInOutline,
  logOutOutline,
  mailOutline,
  medical,
  medicalOutline,
  medkitOutline,
  personAddOutline,
  personCircleOutline,
  personOutline,
  ribbonOutline,
  shieldCheckmarkOutline,
  timeOutline,
} from 'ionicons/icons';

/** Los iconos usados por las pantallas se incluyen en el bundle sin depender de una URL de SVG. */
export function registerAppIcons(): void {
  addIcons({
    alertCircleOutline, arrowBackOutline, arrowForwardOutline, calendarOutline, callOutline,
    checkmarkCircleOutline, documentTextOutline, eyeOffOutline, eyeOutline, heartOutline,
    informationCircleOutline, lockClosedOutline, logInOutline, logOutOutline, mailOutline,
    medical, medicalOutline, medkitOutline, personAddOutline, personCircleOutline,
    personOutline, ribbonOutline, shieldCheckmarkOutline, timeOutline,
  });
}
