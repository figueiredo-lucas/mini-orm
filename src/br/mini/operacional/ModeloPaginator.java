package br.mini.operacional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.mini.criterio.Criterio;
import br.mini.exception.MiniException;

public final class ModeloPaginator<T> {

    private int tamanhoTotal;
    private int pageSize = 10;
    private final Map<Integer, List<T>> valoresMap = new HashMap<>(10);
    private final Operacoes dao = new Operacoes();
    private final Criterio criterio;
    private final Class<T> classe;

    public ModeloPaginator(Class<T> classe, Criterio criterio) throws MiniException {
        this.criterio = criterio;
        this.classe = classe;
        paginar();
    }

    public ModeloPaginator(Class<T> classe, Criterio criterio, int pageSize) throws MiniException {
        this.pageSize = pageSize;
        this.criterio = criterio;
        this.classe = classe;
        paginar();
    }

    public void paginar() throws MiniException {
        tamanhoTotal = dao.processarCount(classe, criterio);
        List<T> valoresList = dao.processarValoresPaginados(classe, criterio, 1, 100);
        popularMapa(valoresList, 1);
    }

    public void repaginar(int inicial) throws MiniException {
        List<T> valoresList = dao.processarValoresPaginados(classe, criterio, inicial, (inicial - 1) + pageSize);
        popularMapa(valoresList, inicial);
    }

    public List<T> repaginar(int inicial, int sessionSize) throws MiniException {
        return dao.processarValoresPaginados(classe, criterio, inicial, (inicial - 1) + sessionSize);
    }

    private void popularMapa(List<T> valoresList, int inicial) {
        if (valoresList != null) {
            int qtd = (int) Math.ceil((float) valoresList.size() / (float) pageSize);
            for (int i = 1; i <= qtd; i++) {
                int paginaAtual = ((inicial - 1) / 10) + 1;
                try {
                    valoresMap.put(paginaAtual, new ArrayList<>(valoresList.subList(((i - 1) * pageSize), i * pageSize)));
                } catch (IndexOutOfBoundsException e) {
                    valoresMap.put(paginaAtual, new ArrayList<>(valoresList.subList(((i - 1) * pageSize), valoresList.size())));
                }

                inicial = inicial + pageSize;
            }
        }
    }

    public int getTamanhoTotal() {
        return tamanhoTotal;
    }

    public List<T> getValoresList(Integer pagina) {
        return valoresMap.get(pagina);
    }

    public Criterio getCriterio() {
        return criterio;
    }
}
