import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ScheduleSearch } from './schedule-search';

describe('ScheduleSearch', () => {
  let component: ScheduleSearch;
  let fixture: ComponentFixture<ScheduleSearch>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScheduleSearch],
    }).compileComponents();

    fixture = TestBed.createComponent(ScheduleSearch);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
