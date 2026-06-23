package service;

import model.NivelAlerta;

public class AlagamentoService {

    public NivelAlerta classificar(double mm24, double mm48, double mm72) {
        if (mm24 >= 80 || mm48 >= 100 || mm72 >= 120) {
            return NivelAlerta.EMERGENCIA;
        }
        if (mm24 >= 50 || mm48 >= 70 || mm72 >= 90) {
            return NivelAlerta.ALERTA;
        }
        if (mm24 >= 30 || mm48 >= 50 || mm72 >= 60) {
            return NivelAlerta.ATENCAO;
        }
        return NivelAlerta.NORMAL;
    }

    public NivelAlerta classificarComLimiar(double mm24, double mm48, double mm72, double limiarMm24) {
        NivelAlerta nivel = classificar(mm24, mm48, mm72);
        if (limiarMm24 > 0 && mm24 >= limiarMm24 && nivel == NivelAlerta.NORMAL) {
            return NivelAlerta.ATENCAO;
        }
        return nivel;
    }
}
