import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VoorzieningService } from '../../services/voorziening.service';
import { AuthService } from '../../services/auth.service';
import { Voorziening } from '../../models/voorziening.model';

@Component({
  selector: 'app-voorzieningen',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './voorzieningen.component.html',
  styleUrls: ['./voorzieningen.component.css'],
})
export class VoorzieningenComponent implements OnInit {
  voorzieningen: Voorziening[] = [];
  isAdmin = false;
  loading = false;
  error = '';
  success = '';

  showForm = false;
  editMode = false;
  currentVoorziening: Voorziening = this.getEmptyVoorziening();

  constructor(
    private voorzieningService: VoorzieningService,
    private authService: AuthService
  ) {}

  async ngOnInit() {
    this.isAdmin = await this.authService.hasRole('admin');
    this.loadVoorzieningen();
  }

  loadVoorzieningen() {
    this.loading = true;
    this.error = '';
    this.voorzieningService.getAll().subscribe({
      next: (data) => {
        this.voorzieningen = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load voorzieningen: ' + err.message;
        this.loading = false;
      },
    });
  }

  openCreateForm() {
    this.showForm = true;
    this.editMode = false;
    this.currentVoorziening = this.getEmptyVoorziening();
  }

  openEditForm(voorziening: Voorziening) {
    this.showForm = true;
    this.editMode = true;
    this.currentVoorziening = { ...voorziening };
  }

  closeForm() {
    this.showForm = false;
    this.currentVoorziening = this.getEmptyVoorziening();
    this.error = '';
    this.success = '';
  }

  save() {
    this.loading = true;
    this.error = '';
    this.success = '';

    if (this.editMode && this.currentVoorziening.id) {
      this.voorzieningService
        .update(this.currentVoorziening.id, this.currentVoorziening)
        .subscribe({
          next: () => {
            this.success = 'Voorziening updated successfully';
            this.loadVoorzieningen();
            this.closeForm();
            this.loading = false;
          },
          error: (err) => {
            this.error = 'Failed to update voorziening: ' + err.message;
            this.loading = false;
          },
        });
    } else {
      this.voorzieningService.create(this.currentVoorziening).subscribe({
        next: () => {
          this.success = 'Voorziening created successfully';
          this.loadVoorzieningen();
          this.closeForm();
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to create voorziening: ' + err.message;
          this.loading = false;
        },
      });
    }
  }

  delete(id: string) {
    if (!confirm('Are you sure you want to delete this voorziening?')) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';

    this.voorzieningService.delete(id).subscribe({
      next: () => {
        this.success = 'Voorziening deleted successfully';
        this.loadVoorzieningen();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to delete voorziening: ' + err.message;
        this.loading = false;
      },
    });
  }

  private getEmptyVoorziening(): Voorziening {
    return {
      naam: '',
      beschrijving: '',
      longitude: 0,
      latitude: 0,
    };
  }
}
