package engineer.mkistoukou.tika.shared;

@FunctionalInterface
public interface UseCase<I, O> {
  O execute(I input);
}
