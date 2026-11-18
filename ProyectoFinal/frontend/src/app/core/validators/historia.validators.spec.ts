import { FormControl, FormGroup } from '@angular/forms';

import { CitaDePaciente, citaDelPaciente, sinSoloEspacios } from './historia.validators';

describe('Validadores de historia clinica', () => {
  describe('sinSoloEspacios (@NotBlank)', () => {
    const validar = sinSoloEspacios();

    it('deja el valor vacio a Validators.required', () => {
      expect(validar(new FormControl(''))).toBeNull();
      expect(validar(new FormControl(null))).toBeNull();
    });

    it('rechaza un texto formado solo por espacios, tabs o saltos de linea', () => {
      expect(validar(new FormControl('   '))).toEqual({ soloEspacios: true });
      expect(validar(new FormControl('\t\n '))).toEqual({ soloEspacios: true });
    });

    it('acepta un texto con contenido aunque tenga espacios alrededor', () => {
      expect(validar(new FormControl('  Dolor en la pieza 46  '))).toBeNull();
    });
  });

  describe('citaDelPaciente (regla entre campos)', () => {
    const citas: CitaDePaciente[] = [
      { id: 10, pacienteId: 1 },
      { id: 20, pacienteId: 2 },
    ];

    function crearGrupo(pacienteId: string, citaId: string): FormGroup {
      return new FormGroup(
        {
          pacienteId: new FormControl(pacienteId),
          citaId: new FormControl(citaId),
        },
        { validators: citaDelPaciente(() => citas) },
      );
    }

    it('no exige cita: sin cita el grupo es valido', () => {
      expect(crearGrupo('1', '').errors).toBeNull();
    });

    it('sin paciente elegido no compara (lo resuelve required)', () => {
      expect(crearGrupo('', '10').errors).toBeNull();
    });

    it('acepta la cita del mismo paciente', () => {
      expect(crearGrupo('1', '10').errors).toBeNull();
    });

    it('rechaza la cita de otro paciente', () => {
      expect(crearGrupo('1', '20').errors).toEqual({ citaDeOtroPaciente: true });
    });

    it('una cita que no conoce la deja al servidor', () => {
      expect(crearGrupo('1', '99').errors).toBeNull();
    });

    it('vuelve a validar cuando cambia el paciente', () => {
      const grupo = crearGrupo('1', '10');
      grupo.controls['pacienteId'].setValue('2');
      expect(grupo.hasError('citaDeOtroPaciente')).toBe(true);
    });
  });
});
