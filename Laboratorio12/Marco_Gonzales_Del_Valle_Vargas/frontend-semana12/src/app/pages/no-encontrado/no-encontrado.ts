import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Destino de la ruta comodin **: cualquier URL que no coincide con otra. */
@Component({
  selector: 'app-no-encontrado',
  imports: [RouterLink],
  templateUrl: './no-encontrado.html',
  styleUrl: './no-encontrado.scss',
})
export class NoEncontrado {}
